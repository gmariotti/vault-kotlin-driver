package com.olx.ps.vault;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultException;
import com.olx.ps.testContainer.VaultContainer;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.time.Duration;

import static io.kotlintest.matchers.MatchersKt.shouldEqual;
import static java.util.Collections.singletonMap;

public class LogicalExtensionsTest {

    @ClassRule
    public static VaultContainer container = new VaultContainer();

    @BeforeClass
    public static void beforeClass()
            throws IOException, InterruptedException, VaultException {
        container.initAndUnsealVault();
        container.setupBackendUserPass();
        container.getVault()
                .auth()
                .loginByUserPass(VaultContainer.USER_ID, VaultContainer.PASSWORD);
    }

    @Test
    public void construct_data_class_from_response() throws VaultException {
        Vault vault = container.getRootVault();
        vault.logical().write("secret/hello", singletonMap("secret", "hello"));
        GenericCredentials expected = new GenericCredentials("hello", Duration.ofHours(24));

        GenericCredentials actual = LogicalExtensions.read(
                vault.logical(),
                "secret/hello",
                GenericCredentials::new
        );
        shouldEqual(actual, expected);
    }
}
