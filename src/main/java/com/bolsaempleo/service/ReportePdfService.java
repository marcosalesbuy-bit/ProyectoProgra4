package com.bolsaempleo.service;

import com.bolsaempleo.model.Puesto;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

/**
 * Generates a PDF report of puestos published in a given month.
 * Uses OpenPDF (free iText fork).
 */
@Service
@RequiredArgsConstructor
public class ReportePdfService {

    private final PuestoService puestoService;

    /**
     * Builds the PDF bytes for all puestos created in anio/mes.
     *
     * @param anio  4-digit year
     * @param mes   month number (1–12)
     * @return raw PDF bytes to write to the HTTP response
     */
    public byte[] generarReporteMensual(int anio, int mes) throws DocumentException {
        List<Puesto> puestos = puestoService.obtenerPorMes(anio, mes);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 54, 36);
        PdfWriter.getInstance(doc, baos);
        doc.open();

        // ── Title ──────────────────────────────────────────────────────────────
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLACK);
        String monthName = Month.of(mes).getDisplayName(TextStyle.FULL, new Locale("es", "CR"));
        doc.add(new Paragraph("Reporte de Puestos — " + monthName + " " + anio, titleFont));
        doc.add(new Paragraph("Total de puestos: " + puestos.size()));
        doc.add(Chunk.NEWLINE);

        if (puestos.isEmpty()) {
            doc.add(new Paragraph("No se registraron puestos en este periodo."));
            doc.close();
            return baos.toByteArray();
        }

        // ── Table ──────────────────────────────────────────────────────────────
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 3, 2, 1.5f, 1.5f});

        addHeader(table, "ID");
        addHeader(table, "Descripción");
        addHeader(table, "Empresa");
        addHeader(table, "Salario");
        addHeader(table, "Tipo");

        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
        for (Puesto p : puestos) {
            table.addCell(new Phrase(String.valueOf(p.getId()), cellFont));

            String desc = p.getDescripcion();
            if (desc.length() > 80) desc = desc.substring(0, 77) + "...";
            table.addCell(new Phrase(desc, cellFont));

            table.addCell(new Phrase(p.getEmpresa().getNombre(), cellFont));

            String salario = p.getSalarioOfrecido() != null
                    ? "₡ " + p.getSalarioOfrecido().toPlainString()
                    : "—";
            table.addCell(new Phrase(salario, cellFont));

            table.addCell(new Phrase(p.isEsPublico() ? "Público" : "Privado", cellFont));
        }

        doc.add(table);
        doc.close();
        return baos.toByteArray();
    }

    private void addHeader(PdfPTable table, String text) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(63, 81, 181));
        cell.setPadding(5);
        table.addCell(cell);
    }
}
