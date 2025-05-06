/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package admin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.table.DefaultTableModel;
import login.LoginForm;
import utilities.PopupWindows;
import voters.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import sqlcommands.SQLConnector;

/**
 *
 * @author Christian
 */
public class AdminForm extends javax.swing.JFrame {

    /**
     * Creates new form PresidentForm
     */
    private Connection conn;
    
    private void loadCandidates() {
        DefaultTableModel model = (DefaultTableModel) tblCandidates.getModel();
        model.setRowCount(0); // Clear existing rows

        String sql = "SELECT * FROM tbl_candidates";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int candidateNo = rs.getInt("candidate_no");
                String name = rs.getString("name");
                String partylist = rs.getString("partylist");
                String position = rs.getString("position");

                model.addRow(new Object[]{candidateNo, name, partylist, position});
            }

        } catch (SQLException e) {
            PopupWindows.errorMessage("Candidate Request Error - 0006", e.getMessage());
        }
    }
    
    private void clearInputs() {
        txtName.setText("");
        txtPartylist.setText("");
        cmbPositions.setSelectedItem("Select Position...");
    }

    
    private void addCandidate() {
        String name = txtName.getText().trim();
        String partylist = txtPartylist.getText().trim();
        String position = cmbPositions.getSelectedItem().toString();

        // Validation
        if (name.isEmpty() || partylist.isEmpty() || position.equals("Select Position...")) {
            PopupWindows.errorMessage("Candidate Request Error - 0007", "Please fill in all fields and select a valid position.");
            
            return;
        }

        String sql = "INSERT INTO tbl_candidates (name, partylist, position) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, partylist);
            stmt.setString(3, position);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                PopupWindows.dialogMessage("Notice", "Candidate Successfully added!");
                loadCandidates(); // Refresh table after insert
                
                clearInputs();    // Clear fields
            }
        } catch (SQLException e) {
            PopupWindows.errorMessage("Candidate Request Error - 0008", e.getMessage());
        }
    }
    
    private void updateCandidate() {
        String id = txtIDNo.getText().trim();
        String name = txtName.getText().trim();
        String partylist = txtPartylist.getText().trim();
        String position = cmbPositions.getSelectedItem().toString();

        // Validation
        if (id.isEmpty() || name.isEmpty() || partylist.isEmpty() || position.equals("Select Position...")) {
            PopupWindows.errorMessage("Candidate Update Error - 0010", "Please select a candidate and complete all fields.");
            
            return;
        }

        String sql = "UPDATE tbl_candidates SET name = ?, partylist = ?, position = ? WHERE candidate_no = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, partylist);
            stmt.setString(3, position);
            stmt.setInt(4, Integer.parseInt(id));

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                PopupWindows.dialogMessage("Notice", "Candidate Successfully updated!");
                loadCandidates(); // Refresh table
                clearInputs();    // Clear inputs
            } else {
                PopupWindows.errorMessage("Candidate Update Error - 0009", "Candidate not found or update failed.");
            }
        } catch (SQLException e) {
            PopupWindows.errorMessage("Candidate Update Error - 0008", e.getMessage());
        }
    }

    private void deleteSelectedCandidate() {
        int selectedRow = tblCandidates.getSelectedRow();

        if (selectedRow == -1) {
            PopupWindows.dialogMessage("No Selection", "Please select a candidate to delete.");
            return;
        }

        String id = tblCandidates.getValueAt(selectedRow, 0).toString();
        String name = tblCandidates.getValueAt(selectedRow, 1).toString();

        int confirm = PopupWindows.optionMessage("Confirm Deletion", "Are you sure you want to delete candidate \"" + name + "\"?");

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            String sql = "DELETE FROM tbl_candidates WHERE candidate_no = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, Integer.parseInt(id));

            int result = pstmt.executeUpdate();

            if (result > 0) {
                PopupWindows.dialogMessage("Candidate Deleted", "Candidate has been successfully deleted.");
                loadCandidates(); // Refresh table
                clearInputs();    // clear text fields
            } else {
                PopupWindows.errorMessage("Candidate Request Error - 0012", "Candidate could not be deleted.");
            }
        } catch (SQLException e) {
            PopupWindows.errorMessage("Candidate Request Error - 0011", e.getMessage());
        }
    }

    private void transferSelectedRowToFields() {
        int selectedRow = tblCandidates.getSelectedRow();

        if (selectedRow != -1) {
            String id = tblCandidates.getValueAt(selectedRow, 0).toString();
            String name = tblCandidates.getValueAt(selectedRow, 1).toString();
            String partylist = tblCandidates.getValueAt(selectedRow, 2).toString();
            String position = tblCandidates.getValueAt(selectedRow, 3).toString();

            txtIDNo.setText(id);
            txtName.setText(name);
            txtPartylist.setText(partylist);
            cmbPositions.setSelectedItem(position);
        }
    }

    private void endVotingAndCountVotes() {
        try {
            // Fetch the vote data from the database
            ResultSet rs = fetchVotesData();

            // Count votes
            Map<String, Integer> presCount = new HashMap<>();
            Map<String, Integer> vpresCount = new HashMap<>();
            Map<String, Integer> senatorCount = new HashMap<>();
            countVotes(rs, presCount, vpresCount, senatorCount);

            // Format and display the result
            String result = formatResult(presCount, vpresCount, senatorCount);
            showResult(result);

            // Save the result to a file
            saveResultToFile(result);

        } catch (IOException | SQLException e) {
            PopupWindows.errorMessage("Vote Counting Error", e.getMessage());
        }
    }

    private ResultSet fetchVotesData() throws SQLException {
        String sql = "SELECT vote_data FROM tblblockchainvotes";
        PreparedStatement pst = conn.prepareStatement(sql);
        return pst.executeQuery();
    }

    private void countVotes(ResultSet rs, Map<String, Integer> presCount, Map<String, Integer> vpresCount, Map<String, Integer> senatorCount) throws SQLException {
        while (rs.next()) {
            String data = rs.getString("vote_data");
            String[] lines = data.split("\n");

            if (lines.length >= 2) {
                // Count votes for President and Vice President
                String presKey = lines[0].replace("President: ", "").trim();
                String vpresKey = lines[1].replace("Vice President: ", "").trim();
                presCount.put(presKey, presCount.getOrDefault(presKey, 0) + 1);
                vpresCount.put(vpresKey, vpresCount.getOrDefault(vpresKey, 0) + 1);
            }

            // Count votes for Senators (from line 3 onwards)
            for (int i = 3; i < lines.length; i++) {
                String senator = lines[i].trim();
                if (!senator.isEmpty()) {
                    senatorCount.put(senator, senatorCount.getOrDefault(senator, 0) + 1);
                }
            }
        }
    }

    private String formatResult(Map<String, Integer> presCount, Map<String, Integer> vpresCount, Map<String, Integer> senatorCount) {
        StringBuilder result = new StringBuilder();

        // Format the result for Presidents
        result.append("=== PRESIDENT RESULT ===\n");
        presCount.forEach((k, v) -> result.append(k).append(": ").append(v).append(" votes\n"));

        // Format the result for Vice Presidents
        result.append("\n=== VICE PRESIDENT RESULT ===\n");
        vpresCount.forEach((k, v) -> result.append(k).append(": ").append(v).append(" votes\n"));

        // Format the result for Senators (Top 12)
        result.append("\n=== SENATOR RESULT (Top 12) ===\n");
        senatorCount.entrySet().stream()
            .sorted((a, b) -> b.getValue() - a.getValue())
            .limit(12)
            .forEach(e -> result.append(e.getKey()).append(": ").append(e.getValue()).append(" votes\n"));

        return result.toString();
    }

    private void showResult(String result) {
        // Show the formatted result in a dialog
        PopupWindows.dialogMessage("Final Vote Count", result);
    }

    private void saveResultToFile(String result) throws IOException {
        // Write the formatted result to a text file
        File file = new File("FinalVoteResult.txt");
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(result);
        }
    }
    
    private void cleanBlockchainVotes() {
        int confirm = PopupWindows.optionMessage(
            "Confirm Start",
            "Are you sure you want to start the voting? This will clean the previous voting process. This process cannot be undone."
        );

        // Ends the method when the answer is no
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            String sql = "DELETE FROM tbl_blockchainvotes";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            int result = pstmt.executeUpdate();

            if (result >= 0) {
                Variables.isVoting = true; // Sets the global variable to true for reference
                
                btnReview.setEnabled(false);
                
                PopupWindows.dialogMessage("Notice", "Voting has started!");
            } else {
                PopupWindows.errorMessage("Blockchain Request Error - 0020", "No records were deleted.");
            }
        } catch (SQLException e) {
            PopupWindows.errorMessage("Blockchain Request Error - 0019", e.getMessage());
        }
    }

    public AdminForm() {
        initComponents();
        
        conn = SQLConnector.connectToDB();
        
        loadCandidates();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblCandidates = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtIDNo = new javax.swing.JTextField();
        txtName = new javax.swing.JTextField();
        txtPartylist = new javax.swing.JTextField();
        btnDel = new javax.swing.JButton();
        lblPresError = new javax.swing.JLabel();
        btnUpdate = new javax.swing.JButton();
        btnAdd = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        cmbPositions = new javax.swing.JComboBox<>();
        btnStart = new javax.swing.JButton();
        btnEnd = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();
        btnReview = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Poppins", 1, 24)); // NOI18N
        jLabel1.setText("CANDIDATES");

        tblCandidates.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        tblCandidates.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "CANDIDATE NO.", "NAME", "PARTYLIST", "POSITION"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblCandidates.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblCandidatesMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblCandidates);
        if (tblCandidates.getColumnModel().getColumnCount() > 0) {
            tblCandidates.getColumnModel().getColumn(0).setMinWidth(120);
            tblCandidates.getColumnModel().getColumn(0).setPreferredWidth(120);
            tblCandidates.getColumnModel().getColumn(0).setMaxWidth(120);
            tblCandidates.getColumnModel().getColumn(2).setMinWidth(150);
            tblCandidates.getColumnModel().getColumn(2).setPreferredWidth(150);
            tblCandidates.getColumnModel().getColumn(2).setMaxWidth(150);
            tblCandidates.getColumnModel().getColumn(3).setMinWidth(100);
            tblCandidates.getColumnModel().getColumn(3).setPreferredWidth(100);
            tblCandidates.getColumnModel().getColumn(3).setMaxWidth(100);
        }

        jLabel2.setFont(new java.awt.Font("Poppins", 1, 14)); // NOI18N
        jLabel2.setText("SELECTED CANDIDATE");

        jLabel3.setText("Candidate No.:");

        jLabel4.setText("Name:");

        jLabel5.setText("Partlist:");

        txtIDNo.setEditable(false);

        btnDel.setFont(new java.awt.Font("Poppins", 1, 12)); // NOI18N
        btnDel.setText("Remove Candidate");
        btnDel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelActionPerformed(evt);
            }
        });

        lblPresError.setFont(new java.awt.Font("Poppins", 2, 12)); // NOI18N
        lblPresError.setForeground(new java.awt.Color(247, 71, 71));
        lblPresError.setText("          ");

        btnUpdate.setFont(new java.awt.Font("Poppins", 1, 12)); // NOI18N
        btnUpdate.setText("Update Candidate");
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        btnAdd.setFont(new java.awt.Font("Poppins", 1, 12)); // NOI18N
        btnAdd.setText("Add Candidate");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        jLabel6.setText("Position:");

        cmbPositions.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select Position...", "President", "Vice President", "Senator" }));

        btnStart.setFont(new java.awt.Font("Poppins", 1, 12)); // NOI18N
        btnStart.setText("START VOTING");
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });

        btnEnd.setFont(new java.awt.Font("Poppins", 1, 12)); // NOI18N
        btnEnd.setText("END VOTING");
        btnEnd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEndActionPerformed(evt);
            }
        });

        btnLogout.setFont(new java.awt.Font("Poppins", 1, 12)); // NOI18N
        btnLogout.setText("Log out");
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });

        btnReview.setFont(new java.awt.Font("Poppins", 1, 12)); // NOI18N
        btnReview.setText("Review Vote");
        btnReview.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReviewActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(lblPresError)
                        .addGap(217, 217, 217)
                        .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel3)
                                .addComponent(jLabel2)
                                .addComponent(jLabel4)
                                .addComponent(jLabel5)
                                .addComponent(jLabel6))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(txtPartylist, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                                .addComponent(txtName, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                                .addComponent(txtIDNo, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                                .addComponent(cmbPositions, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnDel, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnReview, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
                        .addComponent(btnStart, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnEnd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(0, 36, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(lblPresError)
                    .addComponent(btnLogout))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(txtIDNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(txtPartylist, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(cmbPositions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnReview)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnAdd)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnUpdate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnDel)))
                .addGap(43, 43, 43)
                .addComponent(btnStart)
                .addGap(18, 18, 18)
                .addComponent(btnEnd)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelActionPerformed
        deleteSelectedCandidate();
    }//GEN-LAST:event_btnDelActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        updateCandidate();
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        addCandidate();
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed

        cleanBlockchainVotes();
    }//GEN-LAST:event_btnStartActionPerformed

    private void btnEndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEndActionPerformed
        Variables.isVoting = false;
        
        endVotingAndCountVotes();
        
        btnReview.setEnabled(true);
        
        PopupWindows.dialogMessage("Notice", "Voting has ended!");
    }//GEN-LAST:event_btnEndActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        LoginForm login = new LoginForm();
        
        login.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void tblCandidatesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblCandidatesMouseClicked
        transferSelectedRowToFields();
    }//GEN-LAST:event_tblCandidatesMouseClicked

    private void btnReviewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReviewActionPerformed
        endVotingAndCountVotes();
    }//GEN-LAST:event_btnReviewActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AdminForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AdminForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AdminForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AdminForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AdminForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDel;
    private javax.swing.JButton btnEnd;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnReview;
    private javax.swing.JButton btnStart;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JComboBox<String> cmbPositions;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblPresError;
    private javax.swing.JTable tblCandidates;
    private javax.swing.JTextField txtIDNo;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtPartylist;
    // End of variables declaration//GEN-END:variables
}
