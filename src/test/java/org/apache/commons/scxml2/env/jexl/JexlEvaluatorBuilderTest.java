package org.apache.commons.scxml2.env.jexl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JexlEvaluatorBuilderTest {

    @Test
    public void testAddAllowedPackages() {

        final String[] jexlPermissions = new JexlEvaluatorBuilder()
                .addAllowedPackage("org.apache.commons.scxml2")
                .addAllowedPackage("org.apache.commons.logging")
                .getJexlPermissions();

        Assertions.assertEquals(2, jexlPermissions.length);
        Assertions.assertEquals("org.apache.commons.scxml2.*", jexlPermissions[0]);
        Assertions.assertEquals("org.apache.commons.logging.*", jexlPermissions[1]);
    }

    @Test
    public void testAllowedPackagesAndDeniedClasses() {

        final String[] jexlPermissions = new JexlEvaluatorBuilder()
                .addAllowedPackage("org.apache.commons.scxml2")
                .addDeniedClass("org.apache.commons.scxml2.Builtin")
                .addDeniedClass("org.apache.commons.logging.Logger")
                .getJexlPermissions();

        Assertions.assertEquals(3, jexlPermissions.length);
        Assertions.assertEquals("org.apache.commons.scxml2.*", jexlPermissions[0]);
        Assertions.assertEquals("org.apache.commons.scxml2 { Builtin {} }", jexlPermissions[1]);
        Assertions.assertEquals("org.apache.commons.logging { Logger {} }", jexlPermissions[2]);
    }
}