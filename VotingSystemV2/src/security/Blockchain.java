/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package security;

/**
 *
 * @author Christian
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import sqlcommands.SQLConnector;
import utilities.PopupWindows;

public class Blockchain {    
    // The blockchain represented as a list of blocks
    public static ArrayList<Block> chain = new ArrayList<>();

    // Initializes the blockchain with a genesis block if the chain is empty
    public static void initializeChain() {
        if (chain.isEmpty()) {
            chain.add(new Block("Genesis Block", "0")); // Genesis block has no previous hash
        }
    }

    // Adds a new block containing vote data to the blockchain
    public static void addVoteBlock(String voteData) {
        Block previousBlock = chain.get(chain.size() - 1); // Get the last block
        Block newBlock = new Block(voteData, previousBlock.hash); // Create new block with previous hash
        chain.add(newBlock); // Add to chain
        saveBlockToDatabase(newBlock); // Persist in database
    }

    // Saves a block into the tbl_blockchainvotes table in the database
    private static void saveBlockToDatabase(Block block) {
        try (Connection con = SQLConnector.connectToDB()) {
            String sql = "INSERT INTO tbl_blockchainvotes (block_hash, previous_hash, vote_data, timestamp) VALUES (?, ?, ?, ?)";

            PreparedStatement statement = con.prepareStatement(sql);
            statement.setString(1, block.getHash()); // Current block's hash
            statement.setString(2, block.getPreviousHash()); // Hash of previous block
            statement.setString(3, block.getData()); // Vote data
            statement.setTimestamp(4, new Timestamp(System.currentTimeMillis())); // Current timestamp

            statement.executeUpdate(); // Execute the insert
        } catch (Exception e) {
            PopupWindows.errorMessage("Blockchain DB Error", e.getMessage()); // Show error if something fails
        }
    }

    // Verifies the integrity of the blockchain by checking hashes
    public static boolean isChainValid() {
        for (int i = 1; i < chain.size(); i++) {
            Block current = chain.get(i);
            Block previous = chain.get(i - 1);

            // Check current block hash integrity
            if (!current.hash.equals(current.calculateHash()))
                return false;

            // Check current block's link to the previous block
            if (!current.previousHash.equals(previous.hash))
                return false;
        }
        
        return true; // Chain is valid
    }
}