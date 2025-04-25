package lk.dk.apigatewaymiddleware.util;

import lk.dk.apigatewaymiddleware.config.WhitelistConfig;
import org.springframework.stereotype.Component;

@Component
public class UrlWhitelistUtil {

    private final WhitelistConfig whitelistConfig;

    public UrlWhitelistUtil(WhitelistConfig whitelistConfig) {
        this.whitelistConfig = whitelistConfig;
    }

    public boolean isWhitelisted(String requestPath) {
        return whitelistConfig.getUrls().contains(requestPath);
    }
}
