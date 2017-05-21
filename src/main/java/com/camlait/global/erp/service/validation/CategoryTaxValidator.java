package com.camlait.global.erp.service.validation;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.camlait.global.erp.delegate.product.ProductManager;
import com.camlait.global.erp.delegate.tax.TaxManager;
import com.camlait.global.erp.domain.document.business.Tax;
import com.camlait.global.erp.domain.product.ProductCategory;
import com.camlait.global.erp.service.domain.CategoryTax;
import com.camlait.global.erp.validation.Validator;
import com.camlait.global.erp.validation.ValidatorResult;
import com.google.common.collect.Lists;

@Component
public class CategoryTaxValidator implements Validator<CategoryTax, ProductCategory> {

    private final ProductManager productManager;
    private final TaxManager taxManager;

    @Autowired
    public CategoryTaxValidator(ProductManager productManager, TaxManager taxManager) {
        this.productManager = productManager;
        this.taxManager = taxManager;
    }

    @Override
    public ValidatorResult<ProductCategory> validate(CategoryTax toValidate) {
        final List<String> errors = Lists.newArrayList();

        if (errors.isEmpty()) {
            final List<Tax> taxes = toValidate.getTaxCodes().stream().map(t -> {
                return taxManager.retrieveTaxByCode(t);
            }).collect(Collectors.toList());
            ProductCategory c = productManager.retrieveProductCategoryByCode(toValidate.getCategoryCode());
            c.addCategoryToTax(taxes);
            return build(errors, c);
        }
        return build(errors, null);
    }

    @Override
    public ValidatorResult<ProductCategory> build(List<String> errors, ProductCategory result) {
        final ValidatorResult<ProductCategory> vr = new ValidatorResult<ProductCategory>();
        vr.setErrors(errors);
        vr.setResult(result);
        return vr;
    }
}
