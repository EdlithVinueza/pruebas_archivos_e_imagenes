package org.example.analisis.archivospsd.estructura.extractor;

import org.example.analisis.archivospsd.estructura.modelo.CapaPSD;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ExtractorCapasPSD {

    private static final Set<String> CLAVES_AJUSTE = Set.of(
            "brit", "levl", "curv", "expA", "vibA", "hue ", "hue2",
            "blnc", "blwh", "phfl", "chnl", "selc", "thrs", "grdm",
            "nvrt", "post", "tsly", "lrFX", "vmsk", "agnc"
    );
    private static final Set<String> CLAVES_RELLENO = Set.of("SoCo", "GdFl", "PtFl");

    public static List<CapaPSD> extraer(String path) {
        List<CapaPSD> capas = new ArrayList<>();

        try (InputStream is = abrirStream(path)) {
            if (is == null) return capas;

            // Usamos un BufferedInputStream para mejorar el rendimiento de los saltos
            DataInputStream dis = new DataInputStream(new BufferedInputStream(is));

            // 1. Header (26 bytes)
            byte[] sig = new byte[4];
            dis.readFully(sig);
            if (!"8BPS".equals(new String(sig))) return capas;

            int version = dis.readUnsignedShort();
            boolean isPsb = (version == 2);
            skipExacto(dis, 6); // Reservado
            skipExacto(dis, 2); // Canales
            skipExacto(dis, 4); // Alto
            skipExacto(dis, 4); // Ancho
            skipExacto(dis, 2); // Profundidad
            skipExacto(dis, 2); // Modo Color

            // 2. Color Mode Data
            long colorModeLen = readUint32(dis);
            System.out.println("[Debug] Saltando Color Mode Data: " + colorModeLen + " bytes");
            skipExacto(dis, colorModeLen);

            // 3. Image Resources
            long resourceLen = readUint32(dis);
            System.out.println("[Debug] Saltando Image Resources: " + resourceLen + " bytes");
            skipExacto(dis, resourceLen);

            // 4. Layer and Mask Information
            long section4Len = isPsb ? dis.readLong() : readUint32(dis);
            System.out.println("[Debug] Sección 4 (Capas) total: " + section4Len + " bytes");

            if (section4Len <= 0) return capas;

            // ── Layer Info Block ──
            long layerInfoLen = isPsb ? dis.readLong() : readUint32(dis);
            if (layerInfoLen <= 0) return capas;

            short layerCountRaw = dis.readShort();
            int layerCount = Math.abs(layerCountRaw);
            System.out.println("[Debug] Capas detectadas en binario: " + layerCount);

            for (int i = 0; i < layerCount; i++) {
                int top = dis.readInt();
                int left = dis.readInt();
                int bottom = dis.readInt();
                int right = dis.readInt();

                int numCh = dis.readUnsignedShort();
                for (int c = 0; c < numCh; c++) {
                    dis.readShort(); // ID Canal
                    skipExacto(dis, isPsb ? 8 : 4); // Tamaño datos canal
                }

                skipExacto(dis, 4); // Firma "8BIM"
                byte[] blendBytes = new byte[4];
                dis.readFully(blendBytes);
                String blendKey = new String(blendBytes);

                int opacity = dis.readUnsignedByte();
                int clipping = dis.readUnsignedByte();
                int flags = dis.readUnsignedByte();
                dis.readByte(); // Filler

                int extraLen = dis.readInt();
                byte[] extra = new byte[extraLen];
                dis.readFully(extra);

                ExtraParseado ep = parsearExtra(extra);

                capas.add(CapaPSD.builder()
                        .indice(i + 1)
                        .nombre(ep.nombre)
                        .tipo(ep.tipo)
                        .blendModeKey(blendKey.trim())
                        .opacidadRaw(opacity)
                        .visible((flags & 0x02) == 0)
                        .bloqueada((flags & 0x01) != 0)
                        .esClippingMask(clipping == 1)
                        .tieneMascaraCapa(ep.tieneMascara)
                        .tieneEfectos(ep.tieneEfectos)
                        .ancho(right - left)
                        .alto(bottom - top)
                        .offsetX(left)
                        .offsetY(top)
                        .build());
            }

        } catch (Exception e) {
            System.err.println("[ExtractorCapas] Error: " + e.getMessage());
        }

        return capas;
    }

    /**
     * MÉTODO CLAVE: Garantiza que se salten los bytes exactos.
     * El skip() nativo de Java a veces salta menos de lo pedido.
     */
    private static void skipExacto(DataInputStream dis, long n) throws IOException {
        long totalSaltado = 0;
        while (totalSaltado < n) {
            long saltado = dis.skip(n - totalSaltado);
            if (saltado <= 0) {
                // Si skip no funciona, leemos un byte para forzar el avance
                dis.readByte();
                totalSaltado++;
            } else {
                totalSaltado += saltado;
            }
        }
    }

    private static long readUint32(DataInputStream dis) throws IOException {
        return dis.readInt() & 0xFFFFFFFFL;
    }

    private record ExtraParseado(String nombre, boolean tieneMascara, boolean tieneEfectos, CapaPSD.Tipo tipo) {}

    private static ExtraParseado parsearExtra(byte[] extra) {
        String nombre = "<sin nombre>";
        boolean tieneMasc = false;
        boolean tieneEfect = false;
        CapaPSD.Tipo tipo = CapaPSD.Tipo.NORMAL;

        try (DataInputStream d = new DataInputStream(new ByteArrayInputStream(extra))) {
            int maskLen = d.readInt();
            if (maskLen > 0) { tieneMasc = true; d.skipBytes(maskLen); }

            int blendLen = d.readInt();
            if (blendLen > 0) d.skipBytes(blendLen);

            int nameLen = d.readUnsignedByte();
            byte[] nameBytes = new byte[nameLen];
            d.readFully(nameBytes);
            nombre = new String(nameBytes, "UTF-8").trim();

            int pad = (4 - ((1 + nameLen) % 4)) % 4;
            if (pad > 0) d.skipBytes(pad);

            while (d.available() >= 12) {
                byte[] sig = new byte[4];
                d.readFully(sig);
                if (!"8BIM".equals(new String(sig)) && !"8B64".equals(new String(sig))) break;

                byte[] keyB = new byte[4];
                d.readFully(keyB);
                String key = new String(keyB).trim();
                int len = d.readInt();
                int totalLen = len + (len % 2); // Padding par

                if (key.equals("luni")) {
                    int uLen = d.readInt();
                    StringBuilder sb = new StringBuilder();
                    for (int u = 0; u < uLen / 2; u++) sb.append((char) d.readUnsignedShort());
                    nombre = sb.toString().trim();
                    d.skipBytes(totalLen - (uLen + 4));
                } else if (key.equals("lsct")) {
                    tipo = CapaPSD.Tipo.GRUPO;
                    d.skipBytes(totalLen);
                } else {
                    if (CLAVES_AJUSTE.contains(key)) tipo = CapaPSD.Tipo.AJUSTE;
                    if (CLAVES_RELLENO.contains(key)) tipo = CapaPSD.Tipo.RELLENO;
                    if (key.equals("TySh")) tipo = CapaPSD.Tipo.TEXTO;
                    if (key.equals("lfx2")) tieneEfect = true;
                    d.skipBytes(totalLen);
                }
            }
        } catch (Exception ignored) {}
        return new ExtraParseado(nombre, tieneMasc, tieneEfect, tipo);
    }

    private static InputStream abrirStream(String path) throws IOException {
        File f = new File(path);
        return f.exists() ? new FileInputStream(f) : null;
    }
}