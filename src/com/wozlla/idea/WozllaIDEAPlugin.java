package com.wozlla.idea;

import java.util.regex.Pattern;

public class WozllaIDEAPlugin {

    public static boolean isWozllaProject = false;

    public static final String IDENTIFY_FILE_PATH = "Editor/identify";
    public static final String EDITOR_DIR_NAME = "Editor";
    public static final String COMPILE_SHELL_PATH = "Editor/compile.sh";
    public static final String COMPILED_FILE_PATH = "Editor/externals.js";

    public static final String SPRITE_ATLAS_SUFFIX = ".tt.json";
    public static final Pattern IMAGE_PATTERN = Pattern.compile("(.*/)*.+\\.(png|jpg)$", Pattern.CASE_INSENSITIVE);

    public static final String COORDS_IMAGE_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAAKoAAACqCAMAAAAKqCSwAAAAgVBMVEUAAACtaGgAAAAAA" +
            "AAAAAClY2MAAAADAAAEAAC4bm4AAAACAAADAAACAAADAAB/MQADAAADAAAKCwkDAAABenYCVlQGAQBaIgACQkBSHwCnY2MA//b/ZgC" +
            "aXFyRi4nHbVgA1s7oXAAAtrAA7ubOUQCMVFQAmZSaPQDHjIy5ZVC2SAAdWcZyAAAAG3RSTlMA5CQxC9cExUDUGXxIaYz4s1baoPPa+" +
            "sfz+MQ1to2JAAACyklEQVR42u3cyXLbMAyAYZSkKZeLVi9xyTSSrNiJ3/8BC9mOl5l2egwwg/9iH7+BRFInws/nFkApq4I2cI021cV" +
            "VGZzlQFV+XNVRGQZUF9dpbMugzI3649ZiSakQizHtV7VHrIWL9PWr94JU7eYlpdSvirJxF+rrx8fva4lk+8M68KO+vVF+AfaPLwBSS" +
            "S4r3/Yz9LKs7lQWmxUpqnVaK6e0dnY+Ag5F1AglSXWxq33j6y6688HaKAtEqars00v3kvpSgdFBGwtUqc6vU7/p09o7AGMM3FrMvX9" +
            "+vi/OfT/V6m5MfRo7bWHun9Rf8O252O7Tvo0O/tKyyLlYApFMKMY01sGQp1rlVymltVeWOtU0BW4B+75uDHGq1eUhHYpVOpTa0qaaJ" +
            "e5Vta9xt1oa2lSr54Nq2XS1pz5VMPPxb5zWivq7+hD5zUqo/02owCGhYkIFDgkVEypwSKiYUIFDQsWEChwSKiZU4JBQMaECh4SKCRU" +
            "4JFRMqMAhoWJCBQ4JFRMqcEiomFCBQ0LFhAocEiomVOCQUDGhAoeEigkVOCRUTKjAIaFiQgUOCRUTKnBIqJhQgUNCxYQKHBIqJlTgk" +
            "FAxoQKHhIoJFTgkVEyowCGhYkIFDgkVEypwSKiYUIFDQsWEChwSKiZU4NCV+nxJI80u1OvVl42+Xn1Jsi+qCds8dbGb8nZJdKz3qXa" +
            "nXBVVPpbUpwquKYZhMwxFQ3Sod6pVvspDrrwiOtQ7FUyopzzVgepQH6igyipXpQKqPVCdR6p3QDWm1IjUSJNqnXJhpgb8Y8Fo33lNc" +
            "1Xp6KNvc27xJyq0KrIfAKE67dpNzpt2d9rS3aTmdHnM0y7n3UT4OL1dJD/lcyfCO/85q2Kb54aW6MK/Z3VZZawqyR78t0woBhxqQfz" +
            "xn1O+wqF6ugf/81h5DBVAlccj4a+px0yz3ZL97n/OuhAc+eX/B3tnjfTmc1XlAAAAAElFTkSuQmCC";

}
