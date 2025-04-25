package lk.dk.apigatewaymiddleware.enums;

import lombok.Getter;

@Getter
public enum WhitelistUrl {
    ACCOUNT_BALANCE("/api/account/balance"),
    ACCOUNT_BALANCE1("/api/account/balance1");

    private final String url;

    WhitelistUrl(String url) {
        this.url = url;
    }

}
