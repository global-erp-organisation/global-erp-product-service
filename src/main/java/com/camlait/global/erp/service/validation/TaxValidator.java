package com.camlait.global.erp.service.validation;

import java.util.List;

import org.springframework.stereotype.Component;

import com.camlait.global.erp.domain.document.business.Tax;
import com.camlait.global.erp.validation.Validator;
import com.camlait.global.erp.validation.ValidatorResult;
import com.google.common.collect.Lists;

@Component
public class TaxValidator implements Validator<Tax, Tax> {

    @Override
    public ValidatorResult<Tax> validate(Tax toValidate) {
        final List<String> errors = Lists.newArrayList();
        return build(errors, toValidate);
    }

    @Override
    public ValidatorResult<Tax> build(List<String> errors, Tax result) {
        final ValidatorResult<Tax> vr = new ValidatorResult<Tax>();
        vr.setErrors(errors);
        if (!errors.isEmpty()) {
            vr.setResult(result);
        }
        return vr;
    }
}
