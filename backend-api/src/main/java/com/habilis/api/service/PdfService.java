package com.habilis.api.service;

import com.habilis.api.entity.DetallePedido;
import com.habilis.api.entity.Pedido;
import com.habilis.api.repository.PedidoRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Servicio para generar documentos PDF
 */
@Service
public class PdfService {

        @Autowired
        private PedidoRepository pedidoRepository;

        /**
         * Generar factura en PDF para un pedido
         * 
         * @param pedidoId ID del pedido
         * @return byte[] con el PDF generado
         */
        public byte[] generarFacturaPedido(Long pedidoId) {
                Pedido pedido = pedidoRepository.findById(pedidoId)
                                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

                try {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        PdfWriter writer = new PdfWriter(baos);
                        PdfDocument pdfDoc = new PdfDocument(writer);
                        Document document = new Document(pdfDoc);

                        // Colores corporativos
                        DeviceRgb primaryColor = new DeviceRgb(76, 175, 80); // Verde
                        DeviceRgb secondaryColor = new DeviceRgb(33, 150, 243); // Azul

                        // ========== ENCABEZADO ==========
                        Paragraph header = new Paragraph("HÁBILIS")
                                        .setFontSize(28)
                                        .setBold()
                                        .setFontColor(primaryColor)
                                        .setTextAlignment(TextAlignment.CENTER);
                        document.add(header);

                        Paragraph subtitle = new Paragraph("Educación diferente")
                                        .setFontSize(12)
                                        .setItalic()
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setMarginBottom(20);
                        document.add(subtitle);

                        // ========== TÍTULO FACTURA ==========
                        Paragraph titulo = new Paragraph("FACTURA")
                                        .setFontSize(20)
                                        .setBold()
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setMarginTop(10)
                                        .setMarginBottom(20);
                        document.add(titulo);

                        // ========== INFORMACIÓN DEL PEDIDO ==========
                        Table infoPedido = new Table(UnitValue.createPercentArray(new float[] { 1, 1 }))
                                        .setWidth(UnitValue.createPercentValue(100))
                                        .setMarginBottom(20);

                        // Columna izquierda - Datos empresa
                        Cell empresaCell = new Cell()
                                        .setBorder(Border.NO_BORDER)
                                        .add(new Paragraph("HÁBILIS S.L.").setBold().setFontSize(12))
                                        .add(new Paragraph("CIF: B12345678").setFontSize(10))
                                        .add(new Paragraph("C/ Educación, 123").setFontSize(10))
                                        .add(new Paragraph("28001 Madrid, España").setFontSize(10))
                                        .add(new Paragraph("Tel: +34 900 123 456").setFontSize(10));

                        // Columna derecha - Datos pedido
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                        String fechaPedido = pedido.getFechaPedido() != null
                                        ? pedido.getFechaPedido().format(formatter)
                                        : "N/A";

                        Cell pedidoCell = new Cell()
                                        .setBorder(Border.NO_BORDER)
                                        .setTextAlignment(TextAlignment.RIGHT)
                                        .add(new Paragraph("N° Pedido: " + pedido.getId()).setBold().setFontSize(12))
                                        .add(new Paragraph("Fecha: " + fechaPedido).setFontSize(10))
                                        .add(new Paragraph("Estado: " + pedido.getEstado()).setFontSize(10)
                                                        .setFontColor(secondaryColor));

                        infoPedido.addCell(empresaCell);
                        infoPedido.addCell(pedidoCell);
                        document.add(infoPedido);

                        // ========== DATOS DEL CLIENTE ==========
                        Paragraph clienteTitulo = new Paragraph("CLIENTE")
                                        .setFontSize(12)
                                        .setBold()
                                        .setMarginTop(10)
                                        .setMarginBottom(5);
                        document.add(clienteTitulo);

                        Table clienteTable = new Table(UnitValue.createPercentArray(new float[] { 1 }))
                                        .setWidth(UnitValue.createPercentValue(100))
                                        .setMarginBottom(20);

                        Cell clienteCell = new Cell()
                                        .setBorder(Border.NO_BORDER)
                                        .setBackgroundColor(new DeviceRgb(245, 245, 245))
                                        .setPadding(10)
                                        .add(new Paragraph(pedido.getUsuario().getNombreCompleto()).setBold()
                                                        .setFontSize(11))
                                        .add(new Paragraph("Email: " + pedido.getUsuario().getCorreoElectronico())
                                                        .setFontSize(10))
                                        .add(new Paragraph("Teléfono: "
                                                        + (pedido.getUsuario().getMovil() != null
                                                                        ? pedido.getUsuario().getMovil()
                                                                        : "N/A"))
                                                        .setFontSize(10));

                        clienteTable.addCell(clienteCell);
                        document.add(clienteTable);

                        // ========== DETALLE DE PRODUCTOS ==========
                        Paragraph detallesTitulo = new Paragraph("DETALLE DEL PEDIDO")
                                        .setFontSize(12)
                                        .setBold()
                                        .setMarginTop(10)
                                        .setMarginBottom(5);
                        document.add(detallesTitulo);

                        Table detalleTable = new Table(UnitValue.createPercentArray(new float[] { 3, 1, 1, 1 }))
                                        .setWidth(UnitValue.createPercentValue(100));

                        // Cabecera tabla
                        detalleTable.addHeaderCell(new Cell()
                                        .setBackgroundColor(primaryColor)
                                        .setFontColor(ColorConstants.WHITE)
                                        .setBold()
                                        .add(new Paragraph("Producto")));
                        detalleTable.addHeaderCell(new Cell()
                                        .setBackgroundColor(primaryColor)
                                        .setFontColor(ColorConstants.WHITE)
                                        .setBold()
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .add(new Paragraph("Cant.")));
                        detalleTable.addHeaderCell(new Cell()
                                        .setBackgroundColor(primaryColor)
                                        .setFontColor(ColorConstants.WHITE)
                                        .setBold()
                                        .setTextAlignment(TextAlignment.RIGHT)
                                        .add(new Paragraph("Precio")));
                        detalleTable.addHeaderCell(new Cell()
                                        .setBackgroundColor(primaryColor)
                                        .setFontColor(ColorConstants.WHITE)
                                        .setBold()
                                        .setTextAlignment(TextAlignment.RIGHT)
                                        .add(new Paragraph("Subtotal")));

                        // Filas de productos
                        for (DetallePedido detalle : pedido.getDetalles()) {
                                detalleTable.addCell(new Cell().add(new Paragraph(detalle.getProducto().getNombre())));
                                detalleTable.addCell(new Cell()
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .add(new Paragraph(String.valueOf(detalle.getCantidad()))));
                                detalleTable.addCell(new Cell()
                                                .setTextAlignment(TextAlignment.RIGHT)
                                                .add(new Paragraph(
                                                                String.format("%.2f €", detalle.getPrecioUnitario()))));
                                detalleTable.addCell(new Cell()
                                                .setTextAlignment(TextAlignment.RIGHT)
                                                .add(new Paragraph(String.format("%.2f €", detalle.getSubtotal()))));
                        }

                        // Fila total
                        detalleTable.addCell(new Cell(1, 3)
                                        .setBorder(Border.NO_BORDER)
                                        .setBackgroundColor(new DeviceRgb(245, 245, 245))
                                        .setBold()
                                        .setTextAlignment(TextAlignment.RIGHT)
                                        .add(new Paragraph("TOTAL:")));
                        detalleTable.addCell(new Cell()
                                        .setBorder(Border.NO_BORDER)
                                        .setBackgroundColor(new DeviceRgb(245, 245, 245))
                                        .setBold()
                                        .setTextAlignment(TextAlignment.RIGHT)
                                        .setFontSize(14)
                                        .setFontColor(primaryColor)
                                        .add(new Paragraph(String.format("%.2f €", pedido.getTotalPedido()))));

                        document.add(detalleTable);

                        // ========== PIE DE PÁGINA ==========
                        Paragraph footer = new Paragraph("\nGracias por confiar en HÁBILIS\n" +
                                        "Para cualquier consulta: info@habilis.com | +34 900 123 456")
                                        .setFontSize(9)
                                        .setItalic()
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setMarginTop(30)
                                        .setFontColor(ColorConstants.GRAY);
                        document.add(footer);

                        document.close();

                        System.out.println("✅ PDF generado para pedido #" + pedidoId + " (" + baos.size() + " bytes)");

                        return baos.toByteArray();

                } catch (Exception e) {
                        System.err.println("❌ Error generando PDF: " + e.getMessage());
                        throw new RuntimeException("Error generando PDF de factura", e);
                }
        }
}
