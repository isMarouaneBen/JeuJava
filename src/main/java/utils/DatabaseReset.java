package utils;

import dal.DatabaseManager;

public class DatabaseReset {
    public static void main(String[] args) {
        try {
            System.out.println("Début de la réinitialisation de la base de données...");
            
            // Réinitialiser la base de données
            DatabaseManager.resetDatabase();
            
            System.out.println("Base de données réinitialisée avec succès !");
            System.out.println("Le compte administrateur a été recréé :");
            System.out.println("Username : adminensah");
            System.out.println("Password : 1234");
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la réinitialisation : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
