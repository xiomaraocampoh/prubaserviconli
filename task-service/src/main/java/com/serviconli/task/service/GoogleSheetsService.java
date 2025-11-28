package com.serviconli.task.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleSheetsService {

    private static final String APPLICATION_NAME = "Serviconli Task Service";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private Sheets sheetsService;

    // ID de la hoja (lo obtienes de la URL de Google Sheets)
    private static final String SPREADSHEET_ID = "1R38f4eoCI0iLBhJCWHXnrTrToRbJZo5cGk2Cm2oSrjQ";
    private static final String HOJA = "CITAS ARMENIA"; // Nombre exacto de la hoja
    private static final String RANGE = HOJA + "!A1:V"; // Rango que cubre todas las columnas

    @PostConstruct
    public void init() throws IOException, GeneralSecurityException {
        sheetsService = getSheetsService();
    }

    private Sheets getSheetsService() throws IOException, GeneralSecurityException {
        InputStream in = GoogleSheetsService.class.getResourceAsStream("/credentials.json");
        if (in == null) {
            throw new IOException("No se encontró el archivo credentials.json en resources");
        }

        GoogleCredentials credentials = GoogleCredentials.fromStream(in)
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),

                
                JSON_FACTORY,
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName(APPLICATION_NAME).build();
    }

    // 1️⃣ Agregar nueva fila
    public void appendRow(List<Object> values) throws IOException {
        ValueRange body = new ValueRange().setValues(Collections.singletonList(values));
        sheetsService.spreadsheets().values()
                .append(SPREADSHEET_ID, RANGE, body)
                .setValueInputOption("USER_ENTERED")
                .execute();
    }

    // 2️⃣ Actualizar fila completa por ID
    public void updateRow(String tareaId, List<Object> values) throws IOException {
        List<List<Object>> rows = getAllRows();
        if (rows == null) return;

        for (int i = 0; i < rows.size(); i++) {
            List<Object> row = rows.get(i);
            if (!row.isEmpty()) {
                // Verifica el último valor de la fila (ID en columna U)
                String lastValue = row.get(row.size() - 1).toString();
                if (tareaId.equals(lastValue)) {
                    String updateRange = HOJA + "!A" + (i + 1) + ":V" + (i + 1);
                    ValueRange body = new ValueRange().setValues(List.of(values));
                    sheetsService.spreadsheets().values()
                            .update(SPREADSHEET_ID, updateRange, body)
                            .setValueInputOption("USER_ENTERED")
                            .execute();
                    break;
                }
            }
        }
    }


    // 3️⃣ Actualizar celda específica (columna empieza en 1)
    public void updateCell(String tareaId, int columnIndex, String newValue) throws IOException {
        List<List<Object>> rows = getAllRows();
        if (rows == null) return;

        for (int i = 0; i < rows.size(); i++) {
            if (!rows.get(i).isEmpty() && tareaId.equals(rows.get(i).get(0).toString())) {
                String columnLetter = String.valueOf((char) ('A' + columnIndex - 1));
                String updateRange = HOJA + "!" + columnLetter + (i + 1);
                ValueRange body = new ValueRange().setValues(List.of(List.of(newValue)));
                sheetsService.spreadsheets().values()
                        .update(SPREADSHEET_ID, updateRange, body)
                        .setValueInputOption("USER_ENTERED")
                        .execute();
                break;
            }
        }
    }

    // 4️⃣ Eliminar (marcar como eliminada)
    public void deleteRowById(String tareaId) throws IOException {
        List<List<Object>> rows = getAllRows();
        if (rows == null) return;

        for (int i = 0; i < rows.size(); i++) {
            if (!rows.get(i).isEmpty() && tareaId.equals(rows.get(i).get(0).toString())) {
                if (!rows.get(i).isEmpty()) {
                    rows.get(i).set(rows.get(i).size() - 1, "ELIMINADA");
                }
                String updateRange = HOJA + "!A" + (i + 1) + ":V" + (i + 1);
                ValueRange body = new ValueRange().setValues(List.of(rows.get(i)));
                sheetsService.spreadsheets().values()
                        .update(SPREADSHEET_ID, updateRange, body)
                        .setValueInputOption("USER_ENTERED")
                        .execute();
                break;
            }
        }
    }

    // Método helper para obtener todas las filas
    private List<List<Object>> getAllRows() throws IOException {
        ValueRange response = sheetsService.spreadsheets().values()
                .get(SPREADSHEET_ID, RANGE)
                .execute();
        return response.getValues();
    }
}
