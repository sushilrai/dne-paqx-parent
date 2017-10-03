package com.dell.cpsd.paqx.dne.service.model;

public class ValidateProtectionDomainResponse extends TaskResponse {

    private String protectionDomains;

    public ValidateProtectionDomainResponse() {}

    public ValidateProtectionDomainResponse(String protectionDomains) {
        this.protectionDomains = protectionDomains;
    }

    public String getProtectionDomains() {
        return protectionDomains;
    }

    public void setProtectionDomains(String protectionDomains) {
        this.protectionDomains = protectionDomains;
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();

        builder.append("ProtectionDomains{");
        builder.append("protectionDomains=").append(this.protectionDomains);
        builder.append("}");

        return builder.toString();

    }

}
