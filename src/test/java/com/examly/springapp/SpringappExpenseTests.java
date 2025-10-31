package com.examly.springapp;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;




import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = SpringappApplication.class)
@AutoConfigureMockMvc
public class SpringappExpenseTests {

    @Autowired
    private MockMvc mockMvc;

    // === API TESTS ===

    @Test
    void SpringBoot_DevelopCoreAPIsAndBusinessLogic_test_Add_Expense() throws Exception {
        String json = "{\"title\":\"Lunch\",\"amount\":250,\"category\":\"Food\",\"date\":\"2025-08-05\"}";

        mockMvc.perform(post("/api/budget")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void SpringBoot_DevelopCoreAPIsAndBusinessLogic_test_Get_All_Expenses() throws Exception {
        mockMvc.perform(get("/api/budget")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // === DIRECTORY CHECKS ===

    @Test
    void SpringBoot_ProjectAnalysisAndUMLDiagram_test_Controller_Directory_Exists() {
        File dir = new File("src/main/java/com/examly/springapp/controller");
        assertTrue(dir.exists() && dir.isDirectory());
    }

    @Test
    void SpringBoot_ProjectAnalysisAndUMLDiagram_test_Model_Directory_Exists() {
        File dir = new File("src/main/java/com/examly/springapp/model");
        assertTrue(dir.exists() && dir.isDirectory());
    }

    @Test
    void SpringBoot_ProjectAnalysisAndUMLDiagram_test_Repository_Directory_Exists() {
        File dir = new File("src/main/java/com/examly/springapp/repository");
        assertTrue(dir.exists() && dir.isDirectory());
    }

    @Test
    void SpringBoot_ProjectAnalysisAndUMLDiagram_test_Service_Directory_Exists() {
        File dir = new File("src/main/java/com/examly/springapp/service");
        assertTrue(dir.exists() && dir.isDirectory());
    }

    // === FILE CHECKS ===


    @Test
    void SpringBoot_DatabaseAndSchemaSetup_test_ExpenseModel_File_Exists() {
        File file = new File("src/main/java/com/examly/springapp/model/BudgetEntry.java");
        assertTrue(file.exists());
    }

    // === CLASS CHECKS ===






    @Test
    void SpringBoot_DatabaseAndSchemaSetup_test_ExpenseModel_Class_Exists() {
        checkClassExists("com.examly.springapp.model.BudgetEntry");
    }

    // === FIELD CHECKS ===

    
    @Test
    void SpringBoot_DatabaseAndSchemaSetup_test_Expense_Model_Has_Amount_Field() {
        checkFieldExists("com.examly.springapp.model.BudgetEntry", "amount");
    }

    @Test
    void SpringBoot_DatabaseAndSchemaSetup_test_Expense_Model_Has_Category_Field() {
        checkFieldExists("com.examly.springapp.model.BudgetEntry", "category");
    }

    @Test
    void SpringBoot_DatabaseAndSchemaSetup_test_Expense_Model_Has_Date_Field() {
        checkFieldExists("com.examly.springapp.model.BudgetEntry", "date");
    }
    @Test
    void SpringBoot_DatabaseAndSchemaSetup_test_Expense_Model_Has_Description_Field() {
        checkFieldExists("com.examly.springapp.model.BudgetEntry", "description");
    }

    // === REPO INTERFACE CHECK ===


    // === UTILITY METHODS ===

    private void checkClassExists(String className) {
        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            fail("Class " + className + " does not exist.");
        }
    }

    private void checkFieldExists(String className, String fieldName) {
        try {
            Class<?> clazz = Class.forName(className);
            clazz.getDeclaredField(fieldName);
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            fail("Field " + fieldName + " not found in " + className);
        }
    }

    private void checkClassImplementsInterface(String className, String interfaceName) {
        try {
            Class<?> clazz = Class.forName(className);
            Class<?> iface = Class.forName(interfaceName);
            assertTrue(iface.isAssignableFrom(clazz));
        } catch (ClassNotFoundException e) {
            fail("Missing class or interface.");
        }
    }
}
