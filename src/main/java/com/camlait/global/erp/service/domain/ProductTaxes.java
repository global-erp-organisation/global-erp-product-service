package com.camlait.global.erp.service.domain;

import java.util.List;

import com.google.common.collect.Lists;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ProductTaxes {

    private String productCode;
    private List<String> taxCodes = Lists.newArrayList();

    public ProductTaxes() {

    }
}
