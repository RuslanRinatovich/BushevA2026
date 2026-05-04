package com.furniture.service;

import com.furniture.entity.Client;
import com.furniture.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    public Client findById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Клиент не найден"));
    }

    @Transactional
    public Client save(Client client) {
        return clientRepository.save(client);
    }

    @Transactional
    public void deleteById(Long id) {
        clientRepository.deleteById(id);
    }

    public List<Client> searchByName(String name) {
        if (name == null || name.isBlank()) {
            return clientRepository.findAll();
        }
        return clientRepository.findByNameContainingIgnoreCase(name);
    }
}
