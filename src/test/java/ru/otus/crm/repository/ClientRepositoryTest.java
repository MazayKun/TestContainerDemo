package ru.otus.crm.repository;

import junit.framework.TestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.otus.crm.model.Client;
import ru.otus.crm.model.Manager;
import ru.otus.crm.model.TableWithPk;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Sql("classpath:db.scripts/test-scripts.sql")
@SpringBootTest
@Testcontainers
class ClientRepositoryTest extends TestCase {

    private static final Long TEST_CLIENT_ID = 1L;
    private static final String TEST_CLIENT_NAME = "Test Client 1";
    private static final String TEST_CLIENT_NEW_NAME = "Test Client 1 NEW";
    private static final String TEST_CLIENT_INFO = "Client 1 details";
    private static final String TEST_MANAGER_LABEL = "Test Manager 1";
    private static final String TEST_PK_VALUE = "Test PK value 1";

    @Autowired
    ClientRepository clientRepository;
    @Autowired
    ManagerRepository managerRepository;
    @Autowired
    TableWithPkRepository tableWithPkRepository;

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:alpine")
                    .withInitScript("db.migration/V1__initial_schema.sql");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Test
    @Transactional
    void findAllManagersTest() {
        List<Manager> managers = managerRepository.findAll();
        Assertions.assertEquals(3, managers.size());
        Optional<Manager> testMangerOpt = managers.stream()
                .filter(manager -> "1".equals(manager.getId()))
                .findFirst();
        Assertions.assertTrue(testMangerOpt.isPresent());
        Assertions.assertEquals(TEST_MANAGER_LABEL, testMangerOpt.get().getLabel());
        Assertions.assertEquals(1, testMangerOpt.get().getClients().size());
        Assertions.assertEquals(TEST_CLIENT_NAME, testMangerOpt.get().getClients().iterator().next().getName());
        Assertions.assertEquals(TEST_CLIENT_INFO, testMangerOpt.get().getClients().iterator().next().getClientInfo().getInfo());
        Assertions.assertEquals(0, testMangerOpt.get().getClientsOrdered().size());
    }

    @Test
    @Transactional
    void findClientByNameTest() {
        Optional<Client> clientOpt = clientRepository.findByName(TEST_CLIENT_NAME);
        Assertions.assertTrue(clientOpt.isPresent());
        Assertions.assertEquals(TEST_CLIENT_NAME, clientOpt.get().getName());
        Assertions.assertEquals(TEST_CLIENT_INFO, clientOpt.get().getClientInfo().getInfo());
    }

    @Test
    @Transactional
    void findClientByNameIgnoreCaseTest() {
        Optional<Client> clientOpt = clientRepository.findByNameIgnoreCase(TEST_CLIENT_NAME.toLowerCase(Locale.ROOT));
        Assertions.assertTrue(clientOpt.isPresent());
        Assertions.assertEquals(TEST_CLIENT_NAME, clientOpt.get().getName());
        Assertions.assertNull(clientOpt.get().getClientInfo());
    }

    @Test
    @Transactional
    void updateClientNameTest() {
        clientRepository.updateName(TEST_CLIENT_ID, TEST_CLIENT_NEW_NAME);
        Optional<Client> clientOpt =  clientRepository.findById(TEST_CLIENT_ID);
        Assertions.assertTrue(clientOpt.isPresent());
        Assertions.assertEquals(TEST_CLIENT_NEW_NAME, clientOpt.get().getName());
    }

    @Test
    @Transactional
    void insertFindTableWithPkRepositoryTest() {
        TableWithPk.Pk pk = new TableWithPk.Pk("first_part", "second_part");
        tableWithPkRepository.saveEntry(new TableWithPk(pk, TEST_PK_VALUE));
        Optional<TableWithPk> tableWithPkOpt = tableWithPkRepository.findById(pk);
        Assertions.assertTrue(tableWithPkOpt.isPresent());
        Assertions.assertEquals(TEST_PK_VALUE, tableWithPkOpt.get().getValue());
    }

}