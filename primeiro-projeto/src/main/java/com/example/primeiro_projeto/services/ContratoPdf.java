package com.example.primeiro_projeto.services;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import com.example.primeiro_projeto.models.ContratoModel;

/** Documento gerado com os dados persistidos da negociação. */
public final class ContratoPdf {
    private ContratoPdf() {}
    public static byte[] gerar(ContratoModel contrato) {
        var oferta = contrato.getOferta();
        var imovel = oferta.getImovel();
        List<String> linhas = new ArrayList<>(List.of(
                "IMOBILIARIA - CONTRATO #" + contrato.getId(),
                "Documento demonstrativo para o projeto academico.",
                "", "Situacao: " + contrato.getStatus(),
                "Cliente: " + snapshot(contrato.getNomeClienteSnapshot(), oferta.getUsuario().getNome()),
                "Email: " + snapshot(contrato.getEmailClienteSnapshot(), oferta.getUsuario().getEmail()),
                "Imovel #" + imovel.getId() + ": " + snapshot(contrato.getTituloImovelSnapshot(), imovel.getTitulo()),
                "Endereco: " + snapshot(contrato.getEnderecoImovelSnapshot(), imovel.getEndereco()) + ", " + snapshot(contrato.getNumeroImovelSnapshot(), imovel.getNumero()),
                "Negociacao: " + oferta.getTipo(),
                "Valor acordado: R$ " + oferta.getValorProposto().toPlainString(),
                "Oferta #" + oferta.getId(), "",
                "As partes concordam com os dados e o valor acima registrados.",
                "Assinatura de demonstracao registrada mediante autenticacao do cliente.", ""));
        String observacoes = seguro(contrato.getObservacoes());
        linhas.add("Observacoes:");
        for (String linha : observacoes.split("\\R")) {
            while (linha.length() > 85) { linhas.add(linha.substring(0, 85)); linha = linha.substring(85); }
            linhas.add(linha);
        }
        linhas.add(""); linhas.add("Aprovacao: " + data(contrato.getDataAprovacao()));
        linhas.add("Envio: " + data(contrato.getDataEnvio()));
        linhas.add("Assinado por: " + seguro(contrato.getNomeAssinatura()));
        linhas.add("Data da assinatura: " + data(contrato.getDataAssinatura()));

        List<String> formatadas = new ArrayList<>();
        for (String linha : linhas) {
            while (linha.length() > 85) { formatadas.add(linha.substring(0, 85)); linha = linha.substring(85); }
            formatadas.add(linha);
        }
        int paginas = Math.max(1, (formatadas.size() + 46) / 47);
        List<byte[]> objetos = new ArrayList<>();
        objetos.add(bytes("<< /Type /Catalog /Pages 2 0 R >>"));
        StringBuilder filhos = new StringBuilder();
        for (int p = 0; p < paginas; p++) filhos.append(4 + p * 2).append(" 0 R ");
        objetos.add(bytes("<< /Type /Pages /Kids [" + filhos + "] /Count " + paginas + " >>"));
        objetos.add(bytes("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>"));
        for (int p = 0; p < paginas; p++) {
            int streamId = 5 + p * 2;
            objetos.add(bytes("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 3 0 R >> >> /Contents " + streamId + " 0 R >>"));
            StringBuilder conteudo = new StringBuilder("BT /F1 10 Tf 45 795 Td 15 TL\n");
            for (String linha : formatadas.subList(p * 47, Math.min((p + 1) * 47, formatadas.size())))
                conteudo.append("(").append(escapar(linha)).append(") Tj T*\n");
            conteudo.append("ET\nBT /F1 9 Tf 45 45 Td (Pagina ").append(p + 1).append(" de ").append(paginas).append(") Tj ET\n");
            objetos.add(bytes("<< /Length " + bytes(conteudo.toString()).length + " >>\nstream\n" + conteudo + "endstream"));
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(bytes("%PDF-1.4\n"));
        List<Integer> offsets = new ArrayList<>(); offsets.add(0);
        for (int i = 0; i < objetos.size(); i++) {
            offsets.add(out.size()); out.writeBytes(bytes((i + 1) + " 0 obj\n"));
            out.writeBytes(objetos.get(i)); out.writeBytes(bytes("\nendobj\n"));
        }
        int xref = out.size();
        out.writeBytes(bytes("xref\n0 " + (objetos.size() + 1) + "\n0000000000 65535 f \n"));
        for (int i = 1; i < offsets.size(); i++) out.writeBytes(bytes(String.format(java.util.Locale.ROOT, "%010d 00000 n \n", offsets.get(i))));
        out.writeBytes(bytes("trailer\n<< /Size " + (objetos.size() + 1) + " /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF\n"));
        return out.toByteArray();
    }
    private static byte[] bytes(String texto) { return texto.getBytes(java.nio.charset.Charset.forName("windows-1252")); }
    private static String seguro(String texto) { return texto == null ? "-" : texto; }
    private static String snapshot(String salvo, String legado) { return seguro(salvo == null ? legado : salvo); }
    private static String data(java.time.LocalDateTime data) { return data == null ? "-" : data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")); }
    private static String escapar(String texto) { return texto.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)").replaceAll("[\\p{Cntrl}]", " "); }
}
