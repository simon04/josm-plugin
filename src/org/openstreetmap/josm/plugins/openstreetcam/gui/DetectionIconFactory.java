/*
 * Copyright 2019 Grabtaxi Holdings PTE LTE (GRAB), All rights reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be found in the LICENSE file.
 *
 */
package org.openstreetmap.josm.plugins.openstreetcam.gui;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.ImageIcon;
import org.openstreetmap.josm.plugins.openstreetcam.entity.Sign;
import org.openstreetmap.josm.plugins.openstreetcam.util.cnf.IconConfig;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.ImageProvider.ImageSizes;
import org.openstreetmap.josm.tools.JosmRuntimeException;
import com.grab.josm.common.entity.Pair;


/**
 *
 * @author beataj
 * @version $Revision$
 */
public enum DetectionIconFactory {

    INSTANCE;

    private static final String DELIMITER = "/";
    private static final String UNKNOWN_ICON_NAME = "unknown.svg";
    private static final String SIGN_POST_TYPE = "SIGN_POST";
    private static final String SIGN_POST_ICON_NAME = "information--highway-interchange--g1.svg";
    private final Map<String, Pair<ImageIcon, ImageIcon>> iconsMap;


    private DetectionIconFactory() {
        iconsMap = new ConcurrentHashMap<>();
    }

    public ImageIcon getIcon(final Sign sign, final boolean isSelected) {
        String iconName = sign.getType().equals(SIGN_POST_TYPE) ? SIGN_POST_ICON_NAME : sign.getIconName();
        iconName = iconName == null ? IconConfig.getInstance().getDetectionIconsPath() + DELIMITER + UNKNOWN_ICON_NAME
                : iconName;
        final Pair<ImageIcon, ImageIcon> iconPair = iconsMap.computeIfAbsent(iconName,
                n -> new Pair<>(loadIcon(n, ImageSizes.LARGEICON), loadIcon(n, ImageSizes.CURSOR)));
        return isSelected ? iconPair.getSecond() : iconPair.getFirst();
    }

    private ImageIcon loadIcon(final String name, final ImageSizes size) {
        final String iconPath = IconConfig.getInstance().getDetectionIconsPath() + DELIMITER + name;
        ImageIcon icon = null;
        try {
            icon = ImageProvider.get(iconPath, size);
        } catch (final JosmRuntimeException ex) {
            final String defaultIcon = IconConfig.getInstance().getDetectionIconsPath() + DELIMITER + UNKNOWN_ICON_NAME;
            icon = ImageProvider.get(defaultIcon, size);
        }
        return icon;
    }
}