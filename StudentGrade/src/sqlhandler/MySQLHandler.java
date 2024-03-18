/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sqlhandler;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ADMIN
 */
public class MySQLHandler {
    private String server = "GIGABYTE\\SQLEXPRESS";
    private String user = "sa";
    private String password = "c9q9md";
    private String db = "StudentData_Test";
    private int port = 1433;

    private SQLServerDataSource ds;
    private Connection conn;
    private ResultSet resultSet = null;
    private Statement statement = null;
    
    public MySQLHandler() {
        
    }
    
    public SQLServerDataSource getDs() {
        return ds;
    }

    public void setDs(SQLServerDataSource ds) {
        this.ds = ds;
    }

    public Connection getConn() {
        return conn;
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public void setResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    public Statement getStatement() {
        return statement;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }
    
    public void makeConnection() {
        ds = new SQLServerDataSource();
        
        ds.setUser(user);
        ds.setPassword(password);
        ds.setDatabaseName(db);
        ds.setServerName(server);
        ds.setPortNumber(port);
        ds.setTrustServerCertificate(true);
        
        try {
            conn = ds.getConnection();
            try {
                statement = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            } catch (SQLException ex) {
                Logger.getLogger(MySQLHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLServerException ex) {
            Logger.getLogger(MySQLHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean executeStatement(String SQL_Statement) {
        //Note use execute to execute statement
        boolean success = false;
        try {
            statement.execute(SQL_Statement);
            success = true;
        } catch (SQLException ex) {
            Logger.getLogger(MySQLHandler.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return success;
    }
    
    public boolean executeQuery(String SQL_Query) {
        boolean success = false;
        try {
            this.resultSet = statement.executeQuery(SQL_Query);
            /*
            while (resultSet.next()) {
                System.out.println(resultSet.getString(1));
            }
             */
            
            if (resultSet.next()) {
                success = true;
                //System.out.println(resultSet.getString(1));
            } 
        } catch (SQLException ex) {
            Logger.getLogger(MySQLHandler.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return success;
    }
    
    public boolean executeUpdate(String SQL_Update) {
        //Use when INSERT, DELETE, UPDATE or SQL DDL statement
        boolean success = false;
        
        try {
            statement.executeUpdate(SQL_Update);
            success = true;
        } catch (SQLException ex) {
            Logger.getLogger(MySQLHandler.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        return success;
    }
    
    public boolean getBangDiem(String MaHS, String NamHoc, String HocKy) {
        boolean success = false;
        
        String SQL_Statement = "select M.TenMon, D.Mieng, D.[15Phut], D.[1Tiet], D.GiuaKy, D.CuoiKy, D.DiemTB, HK.HocKy, N.NamHoc " +
                               "from HOC_SINH as H, DIEM as D, MON_HOC as M, HOC_KY as HK, NAM_HOC as N " +
                               "where H.MaHS = D.MaHS " +
                               "and H.MaHS = '" + MaHS + "' COLLATE SQL_Latin1_General_CP1_CS_AS " +
                               "and D.MaMon = M.MaMon " +
                               "and D.HocKy = HK.HocKy " +
                               "and HK.HocKy = " + HocKy + " " +
                               "and D.NamHoc = N.NamHoc " +
                               "and N.NamHoc = '" + NamHoc + "' COLLATE SQL_Latin1_General_CP1_CS_AS";
        
        if (executeQuery(SQL_Statement)) {
            success = true;
        }
        
        return success;
    }
    
    public boolean getNamHoc() {
        boolean success = false;
        String SQL_Statement = "select * from NAM_HOC";
        
        if (executeQuery(SQL_Statement)) {
            success = true;
        } 
        
        return success;
    }
    
    public boolean getClassInfo(String classID) {
        boolean success = false;
        //Check database to get the info of the given class ID
        //Step 1: create a query statement
        String SQL_Statement = "select H.* "
                + "from HOC_SINH as H, LOP_HOC as L "
                + "where H.MaLop = L.MaLop and L.MaLop = '" + classID + "' ";
        
        try {
            //Step 2: exectute query
            if (executeQuery(SQL_Statement) && resultSet.next()) {
                /*
                while (resultSet.next()) {
                    System.out.println(resultSet.getString(1));
                }
                */
                success = true;
                resultSet.first();
            } else {
                System.out.println("Unable to execute statement");
                success = false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(MySQLHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return success;
    }
    
    public boolean getGVChuNhiem(String classID) {
        boolean success = false;
        String SQL_Statement = "select G.*"
                + " from GIAO_VIEN as G, CHU_NHIEM as C"
                + " where G.MaGV = C.MaGV and C.MaLop = '" + classID + "'";
        
        if (executeQuery(SQL_Statement)) {
            success = true;
        } else {
            System.out.println("Unable to find GV chu nhiem");
            success = false;
        }
        return success;
    }
    
    public boolean checkLogin(String userName, String password) {
        //Check database to see if userName or password if correct
        //Return false if fail
        boolean success = false;
            
        try {
            
            //Step1: create a temp table to store all the info of all the account in the database
            String SQL_Statement = "select P.MaPH as ID, T.MaTK as MaTK, T.Password "
                    + "into temp "
                    + "from PHU_HUYNH as P, TAI_KHOAN as T "
                    + "where P.MaTK = T.MaTK "
                    + "union "
                    + "select H.MaHS as ID, T.MaTK as MaTK, T.Password "
                    + "from HOC_SINH as H, TAI_KHOAN as T "
                    + "where H.MaTK = T.MaTK "
                    + "union "
                    + "select G.MaGV as ID, T.MaTK as MaTK, T.Password "
                    + "from GIAO_VIEN as G, TAI_KHOAN as T "
                    + "where G.MaTK = T.MaTK ";
            if (!executeStatement(SQL_Statement)) {
                System.out.println("Unable to execute statement");
                SQL_Statement = "drop table temp";
                executeStatement(SQL_Statement);
                return false;
            }
            
            //Step2: check to see if username exist or password is correct then drop the table afterward
            SQL_Statement = "select * from temp "
                    + "where temp.ID = '" + userName + "'" + " COLLATE SQL_Latin1_General_CP1_CS_AS" + " and temp.Password = '" + password + "' COLLATE SQL_Latin1_General_CP1_CS_AS";
             
            if (!executeQuery(SQL_Statement)) {
                System.out.println("Unable to execute query");
                SQL_Statement = "drop table temp";
                executeStatement(SQL_Statement);
                return false;
            }
            resultSet = statement.executeQuery(SQL_Statement);
            
            if (resultSet.next()) { //The account userName and password is correct
                success = true;
            } else {
                success = false;
            }
            
            SQL_Statement = "drop table temp";
            if (!executeStatement(SQL_Statement)) {
                System.out.println("Unable to execute statement");
                return false;
            }
        } catch (SQLException ex) {
            
            Logger.getLogger(MySQLHandler.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return success;
    }
    
    public static void main(String args[]) throws SQLException, UnsupportedEncodingException {
        System.setOut(new PrintStream(System.out, true, "UTF8"));
        MySQLHandler sqlHandler = new MySQLHandler();
        sqlHandler.makeConnection();
        ResultSet rs;
        if (sqlHandler.getBangDiem("HS0001", "2022-2023", "2")) {
            rs = sqlHandler.getResultSet();
            rs.beforeFirst();
            while (rs.next()) {
                System.out.println(rs.getString("TenMon") + " " + rs.getFloat("Mieng") + " "
                + rs.getFloat("15Phut") + " " + rs.getFloat("1Tiet") + " " + rs.getFloat("GiuaKy") + " " + rs.getFloat("CuoiKy")
                + " " + rs.getFloat("DiemTB") + " " + rs.getString("NamHoc") + " " + rs.getString("HocKy"));
                //System.out.println(sqlHandler.resultSet.getString("TenMon"));
            }
        } else {
            System.out.println("Ko the lay bang diem!");
        }
    }
}
