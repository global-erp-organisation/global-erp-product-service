package com.camlait.global.erp.service.domain;

import java.util.Date;

import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import com.camlait.global.erp.domain.helper.MergeHelper;
import com.camlait.global.erp.domain.helper.SerializerHelper;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

/**
 * Entity base class
 * 
 * @author Martin Blaise Signe
 */
@SuppressWarnings({"unchecked"})
@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class BaseEntityModel {

    @ApiModelProperty(hidden = true)
    @Version
    private Integer version;

    @ApiModelProperty(hidden = true)
    private Date createdDate;

    @ApiModelProperty(hidden = true)
    private Date lastUpdatedDate;

    /**
     * Merge the current entity with the one provided as parameter.
     * 
     * @param from
     * @return The merging object;
     * @see MergeHelper
     */
    public <T extends BaseEntityModel> T merge(@NonNull T from) {
        return (T) MergeHelper.mergeDefault(from, this);
    }

    /**
     * Built a JSON representation of the current entity.
     * 
     * @return A string that represents a JSON value for the current entity.
     */
    public String toJson() {
        return SerializerHelper.toJson(this);
    }
}
