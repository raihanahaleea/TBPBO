import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

// Interface untuk Beban Kerja
interface BebanKerja {
    void hitungBebanKerja();  // Menentukan metode yang harus diimplementasikan
}

// Parent class untuk Dosen
class Dosen implements BebanKerja {
    protected String nidn;
    protected String nama;
    protected int sks;

    // Constructor
    public Dosen(String nidn, String nama, int sks) {
        this.nidn = nidn;
        this.nama = nama;
        this.sks = sks;
    }

    // Implementasi metode hitungBebanKerja() dari interface BebanKerja
    @Override
    public void hitungBebanKerja() {
        System.out.println("Beban kerja dosen " + nama + " (NIDN: " + nidn + ") adalah " + sks + " SKS.");
    }

    // Method untuk menampilkan info dosen
    public void tampilkanInfo() {
        System.out.println("NIDN       : " + nidn);
        System.out.println("Nama Dosen : " + nama);
        System.out.println("Jumlah SKS : " + sks);
    }
}

// Subclass untuk Validasi Data
class ValidasiDosen extends Dosen {
    public ValidasiDosen(String nidn, String nama, int sks) {
        super(nidn, nama, sks);
    }

    // Validasi data input
    public void validasiData() throws IllegalArgumentException {
        if (sks < 0) {
            throw new IllegalArgumentException("Jumlah SKS tidak boleh negatif.");
        }
        if (nidn == null || nidn.isEmpty()) {
            throw new IllegalArgumentException("NIDN tidak boleh kosong.");
        }
    }
}

public class ManajemenBebanKerjaDosen {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/manajemenbkd"; // Ganti sesuai DB Anda
    private static final String DB_USER = "postgres";  // Ganti sesuai username DB Anda
    private static final String DB_PASSWORD = "bismillahlulusgis"; // Ganti sesuai password DB Anda

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        // Menyimpan data dosen dalam List (Collection Framework)
        List<Dosen> daftarDosen = new ArrayList<>();

        // Login
        boolean loginBerhasil = false;
        while (!loginBerhasil) {
            System.out.println("+-----------------------------------------------------+");
            System.out.println("Log in ");
            System.out.print("Username (dosen): ");
            String username = scanner.nextLine();
            System.out.print("Password (nidn): ");
            String password = scanner.nextLine();

            if (username.equalsIgnoreCase("dosen") && password.equals("nidn")) {
                System.out.println("Login berhasil");
                loginBerhasil = true;
            } else {
                System.out.println("Login gagal, silakan coba lagi.");
            }
            System.out.println("+-----------------------------------------------------+");
        }

        // Header Sistem
        System.out.println("Selamat Datang di Sistem Manajemen Beban Kerja Dosen");
        Date now = new Date();
        System.out.println("Tanggal dan Waktu : " + dateFormat.format(now));

        // Koneksi ke database PostgreSQL
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            boolean exit = false;
            while (!exit) {
                System.out.println("\nMenu:");
                System.out.println("1. Tambah Dosen (Create)");
                System.out.println("2. Tampilkan Daftar Dosen (Read)");
                System.out.println("3. Tambahkan SKS pada Dosen (Operasi Matematika)");
                System.out.println("4. Perbarui Data Dosen (Update)");
                System.out.println("5. Hapus Data Dosen (Delete)");
                System.out.println("6. Keluar");
                System.out.print("Pilih menu: ");
                int pilihan = scanner.nextInt();
                scanner.nextLine(); // Konsumsi newline

                switch (pilihan) {
                    case 1:
                        // Create
                        System.out.print("Masukkan NIDN         : ");
                        String nidn = scanner.nextLine();
                        System.out.print("Masukkan Nama Dosen   : ");
                        String nama = scanner.nextLine();
                        System.out.print("Masukkan Jumlah SKS   : ");
                        int sks = scanner.nextInt();

                        // Validasi Data
                        ValidasiDosen dosen = new ValidasiDosen(nidn, nama, sks);
                        dosen.validasiData();

                        // Menambahkan dosen ke dalam daftar dosen (menggunakan Collection Framework)
                        daftarDosen.add(dosen);

                        // Insert data ke database
                        String insertSQL = "INSERT INTO dosen (nidn, nama, sks) VALUES (?, ?, ?)";
                        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                            pstmt.setString(1, nidn);
                            pstmt.setString(2, nama);
                            pstmt.setInt(3, sks);
                            pstmt.executeUpdate();
                            System.out.println("Data dosen berhasil ditambahkan.");
                        }

                        // Menampilkan beban kerja dosen
                        dosen.hitungBebanKerja();
                        break;

                    case 2:
                        // Read
                        String selectSQL = "SELECT * FROM dosen";
                        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(selectSQL)) {
                            while (rs.next()) {
                                System.out.println("NIDN       : " + rs.getString("nidn"));
                                System.out.println("Nama Dosen : " + rs.getString("nama"));
                                System.out.println("Jumlah SKS : " + rs.getInt("sks"));
                                System.out.println("+----------------------------------+");
                            }
                        }

                        // Menampilkan daftar dosen dari Collection
                        System.out.println("\nDaftar Dosen dalam Collection:");
                        for (Dosen d : daftarDosen) {
                            d.tampilkanInfo();
                        }
                        break;

                    case 3:
                        // Tambahkan SKS
                        System.out.print("Masukkan NIDN untuk menambah SKS: ");
                        String nidnTambah = scanner.nextLine();
                        System.out.print("Masukkan jumlah SKS yang akan ditambahkan: ");
                        int sksTambah = scanner.nextInt();

                        String tambahSKSSQL = "UPDATE dosen SET sks = sks + ? WHERE nidn = ?";
                        try (PreparedStatement pstmt = conn.prepareStatement(tambahSKSSQL)) {
                            pstmt.setInt(1, sksTambah);
                            pstmt.setString(2, nidnTambah);
                            pstmt.executeUpdate();
                            System.out.println("SKS berhasil ditambahkan.");
                        }
                        break;
                    case 4:
                        // Update
                        System.out.print("Masukkan NIDN untuk diperbarui: ");
                        String nidnUpdate = scanner.nextLine();
                        System.out.print("Masukkan Nama Dosen: ");
                        String namaBaru = scanner.nextLine();
                        System.out.print("Masukkan Jumlah SKS Baru : ");
                        int sksBaru = scanner.nextInt();

                        String updateSQL = "UPDATE dosen SET nama = ?, sks = ? WHERE nidn = ?";
                        try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
                            pstmt.setString(1, namaBaru);
                            pstmt.setInt(2, sksBaru);
                            pstmt.setString(3, nidnUpdate);
                            pstmt.executeUpdate();
                            System.out.println("Data dosen berhasil diperbarui.");
                        }
                        break;
                    case 5:
                        // Delete
                        System.out.print("Masukkan NIDN untuk dihapus: ");
                        String nidnDelete = scanner.nextLine();

                        String deleteSQL = "DELETE FROM dosen WHERE nidn = ?";
                        try (PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
                            pstmt.setString(1, nidnDelete);
                            pstmt.executeUpdate();
                            System.out.println("Data dosen berhasil dihapus.");
                        }
                        break;
                    case 6:
                        exit = true;
                        break;
                    default:
                        System.out.println("Pilihan tidak valid.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Terjadi kesalahan: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}