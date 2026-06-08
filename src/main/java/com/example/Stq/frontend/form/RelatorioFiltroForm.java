package com.example.Stq.frontend.form;

import java.time.LocalDate;

public record RelatorioFiltroForm(
        LocalDate de,
        LocalDate ate,
        String tipo,
        String status
) {}
