package com.serra.piratas.entities;

public enum TipoFruta {

    PARAMECIA("Paramecia"),
    ZOAN("Zoan"),
    ZOAN_ANCESTRAL("Zoan ancestral"),
    ZOAN_MITICA("Zoan mítica"),
    LOGIA("Logia");

    private final String etiqueta;

    TipoFruta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    // Texto para mostrar en las plantillas: ${fruta.tipo.etiqueta}
    public String getEtiqueta() {
        return etiqueta;
    }
}
