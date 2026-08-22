package com.mhesus.api.auth.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String remitente;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarCodigoRecuperacion(String destinatario, String nombre, String codigo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        if (remitente != null && !remitente.isBlank()) {
            mensaje.setFrom(remitente);
        }
        mensaje.setTo(destinatario);
        mensaje.setSubject("MHESUS — Código para restablecer tu contraseña");
        mensaje.setText(
                "Hola " + nombre + ",\n\n" +
                "Recibimos una solicitud para restablecer tu contraseña de administrador en MHESUS.\n\n" +
                "Tu código de verificación es: " + codigo + "\n\n" +
                "Este código vence en 15 minutos. Si tú no solicitaste esto, ignora este correo.\n\n" +
                "— Sistema interno MHESUS"
        );
        mailSender.send(mensaje);
    }

    public void enviarReporteAsistencia(String destinatario, String nombreUsuario, String tipoMarcado, String fecha, String hora) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        if (remitente != null && !remitente.isBlank()) {
            mensaje.setFrom(remitente);
        }
        mensaje.setTo(destinatario);
        mensaje.setSubject("MHESUS — Asistencia: " + nombreUsuario + " marcó " + tipoMarcado);
        mensaje.setText(
                nombreUsuario + " marcó \"" + tipoMarcado + "\".\n\n" +
                "Fecha: " + fecha + "\n" +
                "Hora: " + hora + "\n\n" +
                "— Sistema interno MHESUS"
        );
        mailSender.send(mensaje);
    }
}
