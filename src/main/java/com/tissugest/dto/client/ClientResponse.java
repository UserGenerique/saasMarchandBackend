package com.tissugest.dto.client;

import com.tissugest.entity.Client;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientResponse {
    private Long id;
    private String name;
    private String phone;
    private String notes;
    private Long totalDebt; // solde dû calculé

    public static ClientResponse from(Client c, Long debt) {
        return ClientResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .phone(c.getPhone())
                .notes(c.getNotes())
                .totalDebt(debt != null ? debt : 0L)
                .build();
    }

    public static ClientResponse from(Client c) {
        return from(c, 0L);
    }
}
