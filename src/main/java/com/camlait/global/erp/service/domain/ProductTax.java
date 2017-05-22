package com.camlait.global.erp.service.domain;

import java.util.List;

import com.google.common.collect.Lists;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
public class ProductTax extends BaseEntityModel {

    private String productCode;
    @Builder.Default
    private List<String> taxCodes = Lists.newArrayList();

    public ProductTax() {

    }
}
