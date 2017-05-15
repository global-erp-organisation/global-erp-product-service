package com.camlait.global.erp.service.validation;

import java.util.List;

import org.springframework.stereotype.Component;

import com.camlait.global.erp.domain.product.TaxModel;
import com.camlait.global.erp.validation.Validator;
import com.google.common.collect.Lists;

@Component
public class TaxModelValidator implements Validator<TaxModel>{

    @Override
    public List<String> validate(TaxModel toValidate) {
        final List<String> errors = Lists.newArrayList();
        return errors;
    }

}
