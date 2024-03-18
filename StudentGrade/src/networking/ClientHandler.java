/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import sqlhandler.MySQLHandler;

/**
 *
 * @author ADMIN
 */
public class ClientHandler implements Runnable{
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private OutputStream outStream;
    private ResultSet resultSet;
    private boolean quit = false;
    
    private MySQLHandler SQLHandler;
    
    public ClientHandler(Socket clientSocket) {
        try {
            this.clientSocket = clientSocket;
            
            in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(this.clientSocket.getOutputStream(), true, StandardCharsets.UTF_8);
            
            outStream = clientSocket.getOutputStream();
            
            SQLHandler = new MySQLHandler();
            SQLHandler.makeConnection();
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void close() {
        try {
            clientSocket.close();
            in.close();
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendMessage(String msg) {
        out.println(msg);
    }
    
    public String readMessage() {
        String response = null;
        try {
            if (!clientSocket.isClosed()) {
                response = in.readLine();
                System.out.println("Handler read: " + response);
            } else {
                System.out.println("Socket closed");
            }
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }
    
    public void sendStudentJSONObjectArray() {
        JSONArray jsonStudentArray = new JSONArray();
 
        try {
            this.resultSet.beforeFirst();
            while (resultSet.next()) {
                //Create a JSON object to add to the array
                JSONObject student = new JSONObject();
                student.put("MaHS", resultSet.getString(1));
                student.put("HoTen", resultSet.getString(2));
                student.put("NamSinh", resultSet.getInt(3));
                student.put("HanhKiem", resultSet.getString(4));
                student.put("MaTK", resultSet.getString(5));
                student.put("MaLop", resultSet.getString(6));
                student.put("GioiTinh", resultSet.getString(7));
                
                jsonStudentArray.put(student);
                
                //System.out.println(resultSet.getNString(1) + " " + resultSet.getNString(2));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //Send the JSON array to client in bytes;
        //Use out put stream and not printwriter
        try {
            byte[] bytes = jsonStudentArray.toString().getBytes();
            outStream.write(bytes);
            System.out.println("Done sending Class info");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void sendGVChuNhiem() {
        try {
            // Create a JSON object
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("MaGV", resultSet.getString(1));
            jsonObject.put("HoTen", resultSet.getString(2));
            jsonObject.put("NamSinh", resultSet.getString(3));
            jsonObject.put("MaTK", resultSet.getString(4));
            jsonObject.put("GioiTinh", resultSet.getString(5));
            
            // Convert JSON object to string
            String jsonString = jsonObject.toString();
            
            // Send the string over the socket
            sendMessage(jsonString);
        } catch (SQLException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Done sending GV");
    }
    
    public void sendNamHoc() {
        JSONArray jsonNamHocArray = new JSONArray();
        try {
            this.resultSet.beforeFirst();            
            while (resultSet.next()) {
                // Create a JSON object
                JSONObject jsonObject = new JSONObject();
                
                jsonObject.put("NamHoc", resultSet.getString(1));
                
                jsonNamHocArray.put(jsonObject);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //Send the JSON array to client in bytes;
        try {
            byte bytes[] = jsonNamHocArray.toString().getBytes();
            outStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void sendBangDiem() {
        JSONArray jsonBangDiemArray = new JSONArray();
        
        try {
            this.resultSet.beforeFirst();//Set the ResultSet cursor befor the first row
            while (resultSet.next()) {
                //Create a JSON object to add to the array
                JSONObject diem = new JSONObject();
                
                diem.put("TenMon", resultSet.getString("TenMon"));
                diem.put("Mieng", resultSet.getFloat("Mieng"));
                diem.put("15Phut", resultSet.getFloat("15Phut"));
                diem.put("1Tiet", resultSet.getFloat("1Tiet"));
                diem.put("GiuaKy", resultSet.getFloat("GiuaKy"));
                diem.put("CuoiKy", resultSet.getFloat("CuoiKy"));
                diem.put("DiemTB", resultSet.getFloat("DiemTB"));
                
                jsonBangDiemArray.put(diem);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //Send the JSON array to client in bytes;
         try {
            byte[] bytes = jsonBangDiemArray.toString().getBytes();
            outStream.write(bytes);
            System.out.println("Done sending BangDiem!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void handleMessage(String msg) {
        if (msg.equals(Command.LOGIN.name())) {
            String userName = readMessage();
            String password = readMessage();
            System.out.println(userName + " " + password);
            
            boolean checkLogin = SQLHandler.checkLogin(userName, password);
            //Send true if account and password is correct
            if (checkLogin == true) {
                sendMessage(Command.TRUE.name());
            } 
            else {
                sendMessage(Command.FALSE.name());
            }
        }
        else if (msg.equals(Command.GET_CLASS_INFO.name())) {
            //Receive the classID
            String classID = readMessage();
            System.out.println("ClassID: " + classID);
            
            boolean checkClassInfo = SQLHandler.getClassInfo(classID);
            
            if (checkClassInfo == true) {
                //System.out.println("Success");
                this.resultSet = SQLHandler.getResultSet();
                sendMessage(Command.TRUE.name());                
                sendStudentJSONObjectArray();
                
                SQLHandler.getGVChuNhiem(classID);
                this.resultSet = SQLHandler.getResultSet();
                sendGVChuNhiem();
            } 
            else {
                System.out.println("Unable to get class Info");
                sendMessage(Command.FALSE.name());
            }
        } 
        else if (msg.equals(Command.GET_NAM_HOC.name())) {
            if (SQLHandler.getNamHoc() == true) {
                sendMessage(Command.TRUE.name());
                this.resultSet = SQLHandler.getResultSet();
                sendNamHoc();
            }
            else {
                sendMessage(Command.FALSE.name());
            }
        }
        else if (msg.equals(Command.GET_BANG_DIEM.name())) {//Lay bang diem tu CSDL cua 1 hoc sinh
            String jsonString = readMessage();
            JSONObject jsonObject = new JSONObject(jsonString);
            
            String MaHS = jsonObject.getString("MaHS");
            String NamHoc = jsonObject.getString("NamHoc");
            String HocKy = jsonObject.getString("HocKy");
            
            if (SQLHandler.getBangDiem(MaHS, NamHoc, HocKy)) {
                sendMessage(Command.TRUE.name());
                this.resultSet = SQLHandler.getResultSet();
                
                sendBangDiem();
            } 
            else {
                sendMessage(Command.FALSE.name());
                System.out.println("Khong the xem bang diem!");
            }
        }
    }
    
    @Override
    public void run() {
        //System.out.println("Running");
        while (quit == false) {
            System.out.println("Waiting for new message!");
            String msg = readMessage();
            if (msg != null) {
                //System.out.println("<SERVER>: " + msg);
                handleMessage(msg); 
            }
                      
        }
    }
}
