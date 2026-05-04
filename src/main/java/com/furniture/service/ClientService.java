package com.furniture.service;

import com.furniture.entity.Client;
import com.furniture.repository.ClientRepository;
import com.furniture.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final ShipmentRepository shipmentRepository;  // Добавить

    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    public Client findById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Клиент не найден"));
    }

    public List<Client> search(String search) {
        if (search == null || search.isBlank()) {
            return clientRepository.findAll();
        }
        return clientRepository.findByNameContainingIgnoreCase(search);
    }

    @Transactional
    public Client save(Client client) {
        return clientRepository.save(client);
    }

    @Transactional
    public void deleteById(Long id) {
        Client client = findById(id);

        // Проверка наличия связанных отгрузок
        if (shipmentRepository.findByClient(client).size() > 0) {
            throw new RuntimeException("Невозможно удалить клиента '" + client.getName() +
                    "', так как у него есть отгрузки в системе");
        }

        try {
            clientRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Невозможно удалить клиента, так как он связан с другими записями в системе");
        }
    }
}