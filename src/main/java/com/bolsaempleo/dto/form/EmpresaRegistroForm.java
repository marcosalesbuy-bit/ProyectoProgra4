package com.bolsaempleo.dto.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmpresaRegistroForm {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String localizacion;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    private String correoElectronico;

    private String telefono;

    private String descripcion;
}
