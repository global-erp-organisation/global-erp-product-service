package com.camlait.global.erp.service.controllers;

import static com.camlait.global.erp.domain.helper.SerializerHelper.toJson;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.util.StringUtils;
import com.camlait.global.erp.controller.BaseController;
import com.camlait.global.erp.delegate.product.ProductManager;
import com.camlait.global.erp.domain.product.ProductCategory;
import com.camlait.global.erp.validation.Validator;
import com.camlait.global.erp.validation.ValidatorResult;
import com.google.common.base.Joiner;

import io.swagger.annotations.ApiOperation;

@CrossOrigin
@RestController
@RequestMapping(value = "global/v1/categories/")
public class ProductCategoryService extends BaseController{

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductCategoryService.class);
    private final ProductManager productManager;
    private final Validator<ProductCategory, ProductCategory> categoryValidator;

    @Autowired
    public ProductCategoryService(ProductManager productManager, Validator<ProductCategory, ProductCategory> categoryValidator) {
        this.productManager = productManager;
        this.categoryValidator = categoryValidator;
    }

    
    
    /**
     * Add a product category to the catalog.
     * 
     * @param category Product category to store.
     * @param categoryCode Product category code.
     * @return
     */
    @ApiOperation(httpMethod = "POST", 
            value = "Store a product category into the catalog", 
            notes="Resource that store the provided product category into the catalog.",
            response = ProductCategory.class, 
            nickname="categoryAdd")
    @PostMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> categoryAdd(@RequestBody ProductCategory category) {
        final ValidatorResult<ProductCategory> result = categoryValidator.validate(category);
        final List<String> errors = result.getErrors();

        if (!errors.isEmpty()) {
            LOGGER.error("Bad request. errors = [{}]", Joiner.on('\n').join(errors));
            return ResponseEntity.badRequest().body(genericMessage(errors.toString()));
        }
        final ProductCategory pc = productManager.addProductCategory(category);
        LOGGER.info("Product category successfully added. message = [{}]", pc.toJson());
        return ResponseEntity.status(HttpStatus.CREATED).body(pc.toJson());
    }

    /**
     * Update a product in the catalog.
     * 
     * @param category Product to update.
     * @param categoryCode Target product category code that need to be updated.
     * @return the updated product category.
     */
    @PutMapping(value = "{categoryCode}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> categoryUpdate(@RequestBody ProductCategory category, @PathVariable String categoryCode) {
        if (StringUtils.isNullOrEmpty(categoryCode)) {
            return ResponseEntity.badRequest().body(genericMessage("The target product category code should not be null or empty."));
        }
        final ProductCategory c = productManager.retrieveProductCategory(categoryCode);
        if (c == null) {
            return ResponseEntity.badRequest().body(genericMessage("The product category with the code " + categoryCode + " does not exist."));
        }
        ProductCategory toUpdate = category.merge(c);
        final ValidatorResult<ProductCategory> result = categoryValidator.validate(toUpdate);
        final List<String> errors = result.getErrors();

        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(genericMessage(errors.toString()));
        }
        productManager.updateProductCategory(toUpdate);
        return ResponseEntity.ok(toUpdate.toJson());
    }

    /**
     * Retrieve a product category from the catalog.
     * 
     * @param categoryCode Target product category code that need to be retrieved.
     * @return the product category that belongs to the provided code.
     */
    @GetMapping(value = "{categoryCode}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> categoryGet(@PathVariable String categoryCode) {
        if (StringUtils.isNullOrEmpty(categoryCode)) {
            return ResponseEntity.badRequest().body(genericMessage("The target product category code should not be null or empty."));
         }
        final ProductCategory c = productManager.retrieveProductCategory(categoryCode);
        if (c == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(genericMessage("The product category with the code " + categoryCode + " does not exist in the catalog."));
        }
        return ResponseEntity.ok(c.init().toJson());
    }

    /**
     * Retrieve product categories from the catalog base on the given keyword.
     * 
     * @param keyWord Keyword.
     * @return The collection of product categories that match with provided conditions.
     */
    @GetMapping(value = {"keyword", "keyword/{keyWord}"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> categoryGetByKeyWord(@PathVariable(required = false) Optional<String> keyWord) {
        final String present = keyWord.isPresent() ? keyWord.get() : null;
        final List<ProductCategory> p = productManager.retrieveProductCategories(present);
        return ResponseEntity.ok(toJson(p));
    }

    /**
     * Retrieves product categories from the catalog for a specific category parent.
     * 
     * @param parentCode Target category parent.
     * @return All the product that belong to the provided category code.
     */
    @RequestMapping(value = "category/{parentCode}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public ResponseEntity<String> categoryGetByParent(@PathVariable String parentCode) {
        if (StringUtils.isNullOrEmpty(parentCode)) {
            return ResponseEntity.badRequest().body(genericMessage("The target parent category code should not be null or empty."));
        }
        final List<ProductCategory> categories = productManager.retrieveCategoriesByParent(parentCode);
        return ResponseEntity.ok(toJson(categories));
    }

    /**
     * Delete a product category from the catalog.
     * 
     * @param categoryCode Target product category code that need to be deleted.
     * @return
     */
    @RequestMapping(value = "{categoryCode}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.DELETE)
    public ResponseEntity<String> categoryDelete(@PathVariable String categoryCode) {
        if (StringUtils.isNullOrEmpty(categoryCode)) {
            return ResponseEntity.badRequest().body(genericMessage("The target product category code should not be null or empty."));
        }
        final ProductCategory c = productManager.retrieveProductCategory(categoryCode);
        if (c == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(genericMessage("The product category with the code " + categoryCode + " does not exist in the catalog."));
        }
        final Boolean result = productManager.removeProductCategory(c.getProductCategoryId());
        return result ? ResponseEntity.ok(genericMessage("The product " + c.getCategoryDescription() + " has been succesfully removed."))
                      : ResponseEntity.ok(genericMessage("The product " + c.getCategoryDescription() + " were not succesfully removed."));
    }
}
