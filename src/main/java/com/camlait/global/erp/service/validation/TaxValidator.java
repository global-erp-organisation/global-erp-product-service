package com.camlait.global.erp.service.validation;

import java.util.List;

import org.springframework.stereotype.Component;

import com.camlait.global.erp.domain.document.business.Tax;
import com.camlait.global.erp.validation.Validator;

@Component
public class TaxValidator implements Validator<Tax>{

    @Override
    public List<String> validate(Tax toValidate) {
        // TODO Auto-generated method stub
        return null;
    }

}
