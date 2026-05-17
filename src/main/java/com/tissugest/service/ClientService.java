package com.tissugest.service;

import com.tissugest.dto.client.ClientRequest;
import com.tissugest.dto.client.ClientResponse;
import com.tissugest.entity.Client;
import com.tissugest.entity.Shop;
import com.tissugest.exception.BusinessException;
import com.tissugest.repository.ClientRepository;
import com.tissugest.repository.SaleRepository;
import com.tissugest.security.SecurityHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final SaleRepository saleRepository;
    private final SecurityHelper securityHelper;

    public List<ClientResponse> listByShop() {
        Shop shop = securityHelper.getCurrentShop();
        return clientRepository.findByShopId(shop.getId()).stream()
                .map(c -> ClientResponse.from(c, saleRepository.calculateClientDebt(c.getId())))
                .collect(Collectors.toList());
    }

    public ClientResponse getById(Long id) {
        Shop shop = securityHelper.getCurrentShop();
        Client client = clientRepository.findByIdAndShopId(id, shop.getId())
                .orElseThrow(() -> BusinessException.notFound("Client", id));
        Long debt = saleRepository.calculateClientDebt(client.getId());
        return ClientResponse.from(client, debt);
    }

    @Transactional
    public ClientResponse create(ClientRequest request) {
        Shop shop = securityHelper.getCurrentShop();
        Client client = Client.builder()
                .shop(shop)
                .name(request.getName())
                .phone(request.getPhone())
                .notes(request.getNotes())
                .build();
        client = clientRepository.save(client);
        return ClientResponse.from(client, 0L);
    }

    @Transactional
    public ClientResponse update(Long id, ClientRequest request) {
        Shop shop = securityHelper.getCurrentShop();
        Client client = clientRepository.findByIdAndShopId(id, shop.getId())
                .orElseThrow(() -> BusinessException.notFound("Client", id));
        client.setName(request.getName());
        client.setPhone(request.getPhone());
        client.setNotes(request.getNotes());
        client = clientRepository.save(client);
        Long debt = saleRepository.calculateClientDebt(client.getId());
        return ClientResponse.from(client, debt);
    }

    @Transactional
    public void delete(Long id) {
        Shop shop = securityHelper.getCurrentShop();
        Client client = clientRepository.findByIdAndShopId(id, shop.getId())
                .orElseThrow(() -> BusinessException.notFound("Client", id));
        clientRepository.delete(client);
    }
}
