package org.example.analisis.core.model.forense;

/**
 * Representa el conjunto de evidencias periciales que se empaquetarán en
 * formato JSON
 * y se inyectarán de forma esteganográfica (DCT - Luminancia) y física (EOF) en
 * el archivo final.
 */
public class PayloadForense {
    private String id;
    private String aut; // Nombre Legal
    private String art; // Nombre Artístico
    private String ph; // Perceptual Hash
    private String sha; // SHA-256 de la imagen final
    private String sig_a; // Firma del autor (Base64)
    private String sig_s; // Firma del sistema/CA (Base64)
    private long ts; // Sello de tiempo RFC 3161

    public PayloadForense(String id, String aut, String art, String ph, String sha, String sig_a, String sig_s,
            long ts) {
        this.id = id;
        this.aut = aut;
        this.art = art;
        this.ph = ph;
        this.sha = sha;
        this.sig_a = sig_a;
        this.sig_s = sig_s;
        this.ts = ts;
    }

    public String getId() {
        return id;
    }

    public String getAut() {
        return aut;
    }

    public String getArt() {
        return art;
    }

    public String getPh() {
        return ph;
    }

    public String getSha() {
        return sha;
    }

    public String getSig_a() {
        return sig_a;
    }

    public String getSig_s() {
        return sig_s;
    }

    public long getTs() {
        return ts;
    }

    @Override
    public String toString() {
        return "{" +
                "\"id\":\"" + id + "\"," +
                "\"aut\":\"" + aut + "\"," +
                "\"art\":\"" + art + "\"," +
                "\"ph\":\"" + ph + "\"," +
                "\"sha\":\"" + sha + "\"," +
                "\"sig_a\":\"" + sig_a + "\"," +
                "\"sig_s\":\"" + sig_s + "\"," +
                "\"ts\":" + ts +
                "}";
    }
}
