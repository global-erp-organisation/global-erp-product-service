package com.camlait.global.erp.service.controllers;

import static com.camlait.global.erp.domain.helper.SerializerHelper.toJson;

import java.util.List;
import java.util.stream.Collectors;

import org.h2.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.camlait.global.erp.delegate.product.ProductManager;
import com.camlait.global.erp.domain.product.Product;
import com.camlait.global.erp.domain.product.ProductCategory;
import com.camlait.global.erp.domain.product.ProductCategoryModel;
import com.camlait.global.erp.validation.Validator;
import com.google.common.base.Joiner;

@RestController
@RequestMapping(value = "global/v1/category")
public class ProductCategoryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductCategoryService.class);
    private final ProductManager productManager;
    private final Validator<ProductCategoryModel> categoryValidator;

    @Autowired
    public ProductCategoryService(ProductManager productManager, Validator<ProductCategoryModel> categoryValidator) {
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
    @RequestMapping(value = "/{parentCode}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.POST)
    public ResponseEntity<String> categoryAdd(@RequestBody ProductCategoryModel category, @PathVariable String parentCode) {
        LOGGER.info("Product category to add received. message = [{}]", category.toJson());
        final List<String> errors = categoryValidator.validate(category);
        if (!errors.isEmpty()) {
            LOGGER.error("Bad request. errors = [{}]", Joiner.on('\n').join(errors));
            return ResponseEntity.badRequest().body(Joiner.on('\n').join(errors));
        }
        if (!StringUtils.isNullOrEmpty(parentCode)) {
            final ProductCategory cp = productManager.retrieveProductCategoryByCode(parentCode);
            if (cp == null) {
                return ResponseEntity.badRequest().body("The product category code " + parentCode + " does not exist.");
            }
            category.addParent(ProductCategoryModel.from(cp));
        }

        final ProductCategory c = productManager.addProductCategory(category);
        LOGGER.info("Product category successfully added. message = [{}]", c.toJson());
        return ResponseEntity.ok(c.toJson());
    }

    /**
     * Update a product in the catalog.
     * 
     * @param category Product to update.
     * @param categoryCode Target product category code that need to be updated.
     * @return the updated product category.
     */
    @RequestMapping(value = "/{categoryCode}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.PUT)
    public ResponseEntity<String> categoryUpdate(@RequestBody Product category, @PathVariable String categoryCode) {
        if (StringUtils.isNullOrEmpty(categoryCode)) {
            return ResponseEntity.badRequest().body("The target product category code should not be null or empty.");
        }
        final ProductCategory c = productManager.retrieveProductCategoryByCode(categoryCode);
        if (c == null) {
            return ResponseEntity.badRequest().body("The product category with the code " + categoryCode + " does not exist.");
        }
        final ProductCategory toUpdate = category.merge(c);
        final List<String> errors = categoryValidator.validate(toUpdate);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(Joiner.on('\n').join(errors));
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
    @RequestMapping(value = "/{categoryCode}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public ResponseEntity<String> categoryGet(@PathVariable String categoryCode) {
        if (StringUtils.isNullOrEmpty(categoryCode)) {
            return ResponseEntity.badRequest().body("The target product category code should not be null or empty.");
        }
        final ProductCategory c = productManager.retrieveProductCategoryByCode(categoryCode);
        if (c == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("The product category with the code " + categoryCode + " does not exist in the catalog.");
        }
        return ResponseEntity.ok(c.toJson());
    }

    /**
     * Retrieve product categories from the catalog base on the given keyword.
     * 
     * @param keyWord Keyword.
     * @param page Page number that need to be retrieved
     * @param size Number of items per page that need to be retrieved.
     * @return The collection of product categories that match with provided conditions.
     */
    @RequestMapping(value = "/keyWord/{keyWord}/page/{page}/size/{size}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public ResponseEntity<String> categoryGetByKeyWord(@PathVariable String keyWord, @PathVariable int page, @PathVariable int size) {
        if (StringUtils.isNullOrEmpty(keyWord)) {
            return ResponseEntity.badRequest().body("The keyword should not be null or empty.");
        }
        final Page<ProductCategory> p = productManager.retriveProductCategories(keyWord, new PageRequest(page, size));
        return ResponseEntity.ok(toJson(p.getContent()));
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
            return ResponseEntity.badRequest().body("The target parent category code should not be null or empty.");
        }
        final List<ProductCategory> categories = productManager.retriveProductCategories(null, null).getContent().stream()
                .filter(c -> c.getParentCategory().getProductCategoryCode().equals(parentCode)).collect(Collectors.toList());
        return ResponseEntity.ok(toJson(categories));
    }

    /**
     * Delete a product category from the catalog.
     * 
     * @param categoryCode Target product category code that need to be deleted.
     * @return
     */
    @RequestMapping(value = "/{categoryCode}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.DELETE)
    public ResponseEntity<String> categoryDelete(@PathVariable String categoryCode) {
        if (StringUtils.isNullOrEmpty(categoryCode)) {
            return ResponseEntity.badRequest().body("The target product category code should not be null or empty.");
        }
        final ProductCategory c = productManager.retrieveProductCategoryByCode(categoryCode);
        if (c == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("The product category with the code " + categoryCode + " does not exist in the catalog.");
        }
        final Boolean result = productManager.removeProductCategory(c.getProductCategoryId());
        return result ? ResponseEntity.ok("The product " + c.getCategoryDescription() + " has been succesfully removed.")
                      : ResponseEntity.ok("The product " + c.getCategoryDescription() + " were not succesfully removed.");
    }
}
