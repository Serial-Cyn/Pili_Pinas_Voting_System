/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package voters;

import java.sql.*;
import javax.swing.table.DefaultTableModel;
import sqlcommands.SQLConnector;
import utilities.PopupWindows;

/**
 *
 * @author Christian
 */
public class SenatorsForm extends javax.swing.JFrame {

    /**
     * Creates new form PresidentForm
     */
    Connection conn;
    
    private void displaySenators() {
        DefaultTableModel model = (DefaultTableModel) tblSenators.getModel();
        model.setRowCount(0); // Clear existing rows

        try {
            String sql = "SELECT candidate_no, name, partylist FROM tbl_candidates WHERE position = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "Senator");

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int candidateNo = rs.getInt("candidate_no");
                String name = rs.getString("name");
                String partylist = rs.getString("partylist");

                model.addRow(new Object[]{candidateNo, name, partylist});
            }
        } catch (SQLException e) {
            PopupWindows.errorMessage("Candidate Request Error - 0015", e.getMessage());
        }
    }
    
    private void repopulateTbl(DefaultTableModel modelSelected) {
        // Clear the table before repopulating it with the updated list
        modelSelected.setRowCount(0);

        // Repopulate the table with the senators in Variables.SelectedSenators
        for (String senator : Variables.SelectedSenators) {
            String[] senatorData = senator.split("\\|");
            modelSelected.addRow(new Object[]{senatorData[0], senatorData[1], senatorData[2]});
        }
    }
    
    private void addSenatorToSelected() {
        int row = tblSenators.getSelectedRow();
        if (row < 0)
            return; // No row selected

        DefaultTableModel modelSenators = (DefaultTableModel) tblSenators.getModel();
        DefaultTableModel modelSelected = (DefaultTableModel) tblSelectedSenators.getModel();

        String candidateNo = modelSenators.getValueAt(row, 0).toString();
        String name = modelSenators.getValueAt(row, 1).toString();
        String partylist = modelSenators.getValueAt(row, 2).toString();

        String senatorEntry = candidateNo + "|" + name + "|" + partylist;

        // Check if the senator is already selected
        if (Variables.SelectedSenators.contains(senatorEntry)) {
            PopupWindows.errorMessage("Duplicate Entry", "This senator is already selected.");
            return;
        }

        // Check if we have already selected 12 senators
        if (Variables.SelectedSenators.size() >= 12) {
            PopupWindows.errorMessage("Senator Limit Reached", "You can only select up to 12 senators.");
            return;
        }

        // Add the senator to the list
        Variables.SelectedSenators.add(senatorEntry);

        // Displays the selected senators
        repopulateTbl(modelSelected);
    }

    private void removeSelectedSenator() {
        int row = tblSelectedSenators.getSelectedRow();

        if (row < 0) {
            PopupWindows.errorMessage("Remove Error", "Please select a senator to remove.");
            return;
        }

        // Get the candidateNo of the selected senator to match with the list in Variables
        String candidateNo = tblSelectedSenators.getValueAt(row, 0).toString();
        String name = tblSelectedSenators.getValueAt(row, 1).toString();
        String partylist = tblSelectedSenators.getValueAt(row, 2).toString();

        String senatorEntry = candidateNo + "|" + name + "|" + partylist;

        // Remove the senator from the Variables.SelectedSenators list
        if (Variables.SelectedSenators.contains(senatorEntry)) {
            Variables.SelectedSenators.remove(senatorEntry);
        }

        // Remove the senator from the table
        DefaultTableModel model = (DefaultTableModel) tblSelectedSenators.getModel();
        model.removeRow(row);
    }
    
    private boolean validateVotesBeforeSubmission() {
        if (Variables.PresID == null || Variables.PresID.isEmpty()) {
            PopupWindows.dialogMessage("Missing Vote", "You have not voted for **President**.");
            return false;
        }

        if (Variables.VPresID == null || Variables.VPresID.isEmpty()) {
            PopupWindows.dialogMessage("Missing Vote", "You have not voted for **Vice President**.");
            return false;
        }

        if (Variables.SelectedSenators.size() <= 0) {
            PopupWindows.dialogMessage("Missing Votes", "You must select atleast **1 Senators**.");
            return false;
        }

        return true; // All positions are filled
    }


    public SenatorsForm() {
        initComponents();

        conn = SQLConnector.connectToDB();
        
        displaySenators();
        
        repopulateTbl((DefaultTableModel) tblSelectedSenators.getModel());
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
        tblSenators = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        btnFinish = new javax.swing.JButton();
        lblSenatorError = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblSelectedSenators = new javax.swing.JTable();
        btnFinish1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Poppins", 1, 24)); // NOI18N
        jLabel1.setText("SENATORS");

        tblSenators.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        tblSenators.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "CANDIDATE NO.", "NAME", "PARTYLIST"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblSenators.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblSenatorsMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblSenators);
        if (tblSenators.getColumnModel().getColumnCount() > 0) {
            tblSenators.getColumnModel().getColumn(0).setMinWidth(120);
            tblSenators.getColumnModel().getColumn(0).setPreferredWidth(120);
            tblSenators.getColumnModel().getColumn(0).setMaxWidth(120);
            tblSenators.getColumnModel().getColumn(2).setMinWidth(150);
            tblSenators.getColumnModel().getColumn(2).setPreferredWidth(150);
            tblSenators.getColumnModel().getColumn(2).setMaxWidth(150);
        }

        jLabel2.setFont(new java.awt.Font("Poppins", 1, 14)); // NOI18N
        jLabel2.setText("SELECTED CANDIDATES");

        btnFinish.setFont(new java.awt.Font("Poppins", 1, 12)); // NOI18N
        btnFinish.setText("Submit Vote!");
        btnFinish.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFinishActionPerformed(evt);
            }
        });

        lblSenatorError.setFont(new java.awt.Font("Poppins", 2, 12)); // NOI18N
        lblSenatorError.setForeground(new java.awt.Color(247, 71, 71));
        lblSenatorError.setText("          ");

        tblSelectedSenators.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        tblSelectedSenators.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "CANDIDATE NO.", "NAME", "PARTYLIST"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(tblSelectedSenators);
        if (tblSelectedSenators.getColumnModel().getColumnCount() > 0) {
            tblSelectedSenators.getColumnModel().getColumn(0).setMinWidth(120);
            tblSelectedSenators.getColumnModel().getColumn(0).setPreferredWidth(120);
            tblSelectedSenators.getColumnModel().getColumn(0).setMaxWidth(120);
            tblSelectedSenators.getColumnModel().getColumn(2).setMinWidth(150);
            tblSelectedSenators.getColumnModel().getColumn(2).setPreferredWidth(150);
            tblSelectedSenators.getColumnModel().getColumn(2).setMaxWidth(150);
        }

        btnFinish1.setFont(new java.awt.Font("Poppins", 1, 12)); // NOI18N
        btnFinish1.setText("Remove Candidate");
        btnFinish1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFinish1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(lblSenatorError))
                    .addComponent(jLabel2)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnFinish1, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(260, 260, 260)
                        .addComponent(btnFinish, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(lblSenatorError))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnFinish)
                    .addComponent(btnFinish1))
                .addContainerGap(28, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnFinishActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFinishActionPerformed
        if (!validateVotesBeforeSubmission()) {
            return; // Stop submission process
        }
        
        Summary summary = new Summary();
        
        summary.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnFinishActionPerformed

    private void btnFinish1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFinish1ActionPerformed
        removeSelectedSenator();
    }//GEN-LAST:event_btnFinish1ActionPerformed

    private void tblSenatorsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblSenatorsMouseClicked
        addSenatorToSelected();
    }//GEN-LAST:event_tblSenatorsMouseClicked

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
            java.util.logging.Logger.getLogger(SenatorsForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SenatorsForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SenatorsForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SenatorsForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SenatorsForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnFinish;
    private javax.swing.JButton btnFinish1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblSenatorError;
    private javax.swing.JTable tblSelectedSenators;
    private javax.swing.JTable tblSenators;
    // End of variables declaration//GEN-END:variables
}
