/*
 * The code is licensed under the LGPL Version 3 license http://www.gnu.org/licenses/lgpl-3.0.en.html.
 * The collected imagery is protected & available under the CC BY-SA version 4 International license.
 * https://creativecommons.org/licenses/by-sa/4.0/legalcode.
 *
 * Copyright (c)2017, Telenav, Inc. All Rights Reserved
 */
package org.openstreetmap.josm.plugins.openstreetcam.argument;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.openstreetmap.josm.data.UserIdentityManager;
import org.openstreetmap.josm.plugins.openstreetcam.service.apollo.DetectionFilter;
import com.telenav.josm.common.entity.EntityUtil;


/**
 * Defines the filters that the user can use to reduce the displayed results.
 *
 * @author beataj
 * @version $Revision$
 */
public class SearchFilter {

    public static final SearchFilter DEFAULT =
            new SearchFilter(null, false, Arrays.asList(ImageDataType.values()), DetectionFilter.DEFAULT);

    private final Date date;
    private final boolean olnyUserData;
    private List<ImageDataType> dataTypes;
    private DetectionFilter detectionFilter;


    /**
     * Builds a new filter with the given arguments.
     *
     * @param date a {@code Date} represents the starting date from which the data is returned
     * @param olnyUserData if true, then only the data contributed by the logged in user is returned
     */
    public SearchFilter(final Date date, final boolean olnyUserData) {
        this.date = date;
        this.olnyUserData = olnyUserData;
    }

    /**
     * Builds a new filter with the given arguments.
     *
     * @param date a {@code Date} represents the starting date from which the data is returned
     * @param onlyMineFlag if true, then only the data contributed by the logged in user is returned
     * @param dataTypes a {@code DataType} specifies the type of data to be displayed
     * @param detectionFilter a {@code DetectionFilter} specifies the {@code Detection} related filters
     */
    public SearchFilter(final Date date, final boolean onlyMineFlag, final List<ImageDataType> dataTypes,
            final DetectionFilter detectionFilter) {
        this(date, onlyMineFlag);
        this.dataTypes = dataTypes;
        this.detectionFilter = detectionFilter;
    }

    public List<ImageDataType> getDataTypes() {
        return dataTypes;
    }

    public DetectionFilter getDetectionFilter() {
        return detectionFilter;
    }

    public Date getDate() {
        return date;
    }

    public boolean isOlnyUserData() {
        return olnyUserData;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + EntityUtil.hashCode(date);
        result = prime * result + EntityUtil.hashCode(olnyUserData);
        result = prime * result + EntityUtil.hashCode(dataTypes);
        result = prime * result + EntityUtil.hashCode(detectionFilter.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        boolean result = false;
        if (this == obj) {
            result = true;
        } else if (obj != null && obj.getClass() == this.getClass()) {
            final SearchFilter other = (SearchFilter) obj;
            result = EntityUtil.bothNullOrEqual(date, other.getDate());
            result = result && (olnyUserData == other.isOlnyUserData());
            result = result && EntityUtil.bothNullOrEqual(dataTypes, other.getDataTypes());
            result = result && EntityUtil.bothNullOrEqual(detectionFilter, other.getDetectionFilter());
        }
        return result;
    }


    /**
     * Verifies if only the detection filters have been changed or not.
     *
     * @param other a {@code SearchFilter} a new search filter
     * @return true/false
     */
    public boolean onlyDetectionFilterChanged(final SearchFilter other) {
        return EntityUtil.bothNullOrEqual(date, other.getDate()) && (olnyUserData == other.isOlnyUserData())
                && EntityUtil.bothNullOrEqual(dataTypes, other.getDataTypes())
                && !EntityUtil.bothNullOrEqual(detectionFilter, other.getDetectionFilter());
    }

    /**
     * Returns the OSM user identifier of the currently logged in user. If the user is not logged in the method return
     * null.
     *
     * @return a {@code Long}
     */
    public Long getOsmUserId() {
        return (isOlnyUserData() && UserIdentityManager.getInstance().isFullyIdentified()
                && UserIdentityManager.getInstance().asUser().getId() > 0)
                ? UserIdentityManager.getInstance().asUser().getId() : null;
    }
}