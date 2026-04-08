package models.dao;

import java.io.*;
import java.nio.file.*;
import models.entities.User;

public class UserDAO {

    private static final String BASE_PATH = "src/txtFiles/";

    // ─── 原有方法：username + password 登录（Staff用）────────────────────────

    public User authenticate(String username, String password, String role) {
        String fileName = getFileByRole(role);
        if (fileName == null) return null;

        Path path = Paths.get(BASE_PATH + fileName);
        if (!Files.exists(path)) {
            System.err.println("File not found: " + path.toAbsolutePath());
            return null;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (firstLine) {
                    firstLine = false;
                    if (line.toLowerCase().startsWith("userid|")) continue;
                }

                String[] parts = line.split("\\|", -1);

                if ("Customer".equals(role)) {
                    User u = parseCustomerByUsername(parts, username, password);
                    if (u != null) return u;
                } else {
                    User u = parseStaff(parts, username, password, role);
                    if (u != null) return u;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ─── 新方法：email + password 登录（Staff用，Customer不走这里）───────────

    public User authenticateByEmail(String email, String password, String role) {
        String fileName = getFileByRole(role);
        if (fileName == null) return null;

        Path path = Paths.get(BASE_PATH + fileName);
        if (!Files.exists(path)) {
            System.err.println("File not found: " + path.toAbsolutePath());
            return null;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (firstLine) {
                    firstLine = false;
                    if (line.toLowerCase().startsWith("userid|")) continue;
                }

                String[] parts = line.split("\\|", -1);

                // Staff 格式: UserID|username|password|role|createdDate
                // 没有 email 字段，所以 staff 暂时用 username 当 email 字段登录
                // 如果以后 staff txt 加了 email，这里改对应 parts 索引即可
                User u = parseStaffByEmail(parts, email, password, role);
                if (u != null) return u;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ─── Staff parser：username 登录 ─────────────────────────────────────────
    // Format: UserID|username|password|role|createdDate

    private User parseStaff(String[] parts, String username, String password, String role) {
        if (parts.length < 5) return null;

        String userId      = parts[0].trim();
        String fileUser    = parts[1].trim();
        String filePass    = parts[2].trim();
        String fileRole    = parts[3].trim();
        String createdDate = parts[4].trim();

        if (fileUser.equals(username) && filePass.equals(password) && fileRole.equals(role)) {
            return new User(userId, fileUser, filePass, null, null, fileRole, "Active", createdDate);
        }
        return null;
    }

    // ─── Staff parser：email 登录（目前用 username 字段当 email）─────────────
    // Format: UserID|username|password|role|createdDate
    // Staff txt 暂时没有 email 字段，所以这里 email 对比 username 字段
    // 如果日后 staff txt 加了 email 列，把 parts[1] 改成对应的 email 索引

    private User parseStaffByEmail(String[] parts, String email, String password, String role) {
        if (parts.length < 5) return null;

        String userId      = parts[0].trim();
        String fileUser    = parts[1].trim();  // username（暂代 email）
        String filePass    = parts[2].trim();
        String fileRole    = parts[3].trim();
        String createdDate = parts[4].trim();

        // 用 username 字段跟 email 比对（staff 没有真实 email 字段）
        if (fileUser.equals(email) && filePass.equals(password) && fileRole.equals(role)) {
            return new User(userId, fileUser, filePass, null, null, fileRole, "Active", createdDate);
        }
        return null;
    }

    // ─── Customer parser：username 登录 ──────────────────────────────────────
    // Format: UserID|username|password|contactNum|email|role|status|createdDate

    private User parseCustomerByUsername(String[] parts, String username, String password) {
        if (parts.length < 8) return null;

        String userId      = parts[0].trim();
        String fileUser    = parts[1].trim();
        String filePass    = parts[2].trim();
        String contactNum  = parts[3].trim();
        String email       = parts[4].trim();
        String fileRole    = parts[5].trim();
        String status      = parts[6].trim();
        String createdDate = parts[7].trim();

        if (fileUser.equals(username)
                && filePass.equals(password)
                && fileRole.equals("Customer")
                && status.equalsIgnoreCase("Approved")) {
            return new User(userId, fileUser, filePass, contactNum, email, fileRole, status, createdDate);
        }
        return null;
    }

    // ─── Role → file mapping ──────────────────────────────────────────────────

    private String getFileByRole(String role) {
        switch (role) {
            case "Manager":      return "manager.txt";
            case "CounterStaff": return "counterStaff.txt";
            case "Technician":   return "technician.txt";
            case "Customer":     return "customer.txt";
            default:             return null;
        }
    }
}