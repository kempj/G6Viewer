
/*
 *  G6ViewPlus.java
 *
 *  Created on Jun 26, 2009, 4:17:10 PM
 *  By:  Hooman Hematti
 *      Justin D'souze
 *      Jeremy Kemp
 *  For Dr. Ermalinda Delavina
 *      Delavinae@uhd.edu
 *
 *  This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

//for file IO
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Vector;//added by Jeremy
import javax.swing.DefaultListModel;
import javax.imageio.*;
import javax.swing.JOptionPane;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;//Jeremy
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.JPanel;
import javax.swing.table.AbstractTableModel;
import java.awt.Dimension;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;//JK
import javax.swing.AbstractListModel;//JK
import javax.swing.ComboBoxModel;//JK
import javax.swing.JTextField;//JK
import java.sql.*;//JK


public class G6ViewPlus extends javax.swing.JFrame
{

    private DefaultMutableTreeNode rootNode;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode[] graphNode;
    private DefaultMutableTreeNode[][] invCatLeaf;

    private ActionListener select;
    private boolean selection;
    static String[] select_array = new String[1000];
    private int x = 0;
    private int digit1 = 0;
    private int digit2 = 0;
    private int digit3 = 0;
    static int[] number_array = new int[1000];
    private ActionListener refresh;
    private boolean DEBUG = false;

    //Jeremys stuff
    //Number of invariants in the Invariant file
    int invCount = 0;
    //Number of invariants in the MCM file
    int colCount = 0;
    //Number of graphs in an invariant file
    int rowCount = 0;
    // large object where all invariant data is stored
    Object[][] data = {{"You","Shouldn't"},{"See","this"}};
   
    //big list of all of the invariants in the mcm file
    public String[] inputInv;
    //model for the list, intermediate place for invariants for driver file
    private DefaultListModel graphListModel = new DefaultListModel();

    //The program should behave properly if one of these elements is removed, or another added.
    //These are used for the tree and building the driver file
    private String[] graphTypeTitle = {"Original Graph","Complement of Graph",
                          "2nd Power of Graph","2-Core of Graph"};

    private String[] invCatTitle = {"Basic Invariants","Degree Invariants",
                       "Distance Invariants","Vertex Subsets",
                       "Invariants on Edges","Subgraph Invariants",
                       "Properties"};
    //data structre where are all the invariants for the driver file go
    public Vector[][] arrayG = new Vector[graphTypeTitle.length][invCatTitle.length];

    public String[] DBcols;
    public String[] opArray = {"+","-","*","/"};

    public class MyComboBoxModel extends AbstractListModel implements ComboBoxModel{
        private Object selectedObject = "Select Attribute";
        public void setSelectedItem(Object item) {
            selectedObject = item;
            fireContentsChanged(this, -1, -1);
        }
        public Object getSelectedItem() {
            return selectedObject;
        }

        public Object getElementAt(int i) {
            if(DBcols[i] != null && i <= DBcols.length){
                return DBcols[i];
            }
            return null;
        }
        public int getSize() {
            if(DBcols != null){
                return DBcols.length;
            }
            return 0;
        }

    }

        public class opBoxModel extends AbstractListModel implements ComboBoxModel{
        private Object selectedObject = "+";
        public void setSelectedItem(Object item) {
            selectedObject = item;
            fireContentsChanged(this, -1, -1);
        }
        public Object getSelectedItem() {
            return selectedObject;
        }

        public Object getElementAt(int i) {
            if(opArray[i] != null && i <= opArray.length){
                return opArray[i];
            }
            return null;
        }
        public int getSize() {
            if(opArray != null){
                return opArray.length;
            }
            return 0;
        }

    }
    //TODO: convert to array
    public MyComboBoxModel[] boxModel= new MyComboBoxModel[12];
    public JTextField[] attNumField = new JTextField[12];
    public opBoxModel[] opModel= new opBoxModel[12];

    public class MyTableModel extends AbstractTableModel {
        public String[] Columns = {"Bad", "Columns"};
        public Object[][] dispData = {{"You","Shouldn't"},{"See","this"}};

         public String getColumnName(int col) {
        return Columns[col].toString();
        }

        public int getColumnCount() {
            return Columns.length;
        }
        public int getRowCount() {
            return dispData.length;
        }
        public void update(String[] Cols, Object[][] dataIn){
            Columns = Cols;
            dispData = dataIn;
            fireTableStructureChanged();
            fireTableDataChanged();
        }
        public Object getValueAt(int row, int col) {
            return dispData[row][col];
        }
    }

    public MyTableModel tModel1 = new MyTableModel();

    public void showSubsetData(int startIndex, int endIndex)
    {
        String[] columnNames = {"test","values"};
        Object[][] dataSmall;

        //~~Shouldn't need these
        if(startIndex > endIndex || startIndex < 0)
        {
            endIndex = 10;
            startIndex = 0;
        }

        if(endIndex > colCount){
            endIndex = colCount - 1;
            startIndex = colCount - 11;
        }
        //Error checking should be done before this is called
        
        dataSmall = new Object[rowCount][endIndex-startIndex + 1];
        columnNames = new String[endIndex-startIndex + 1];
        columnNames[0] = " Graph Names ";

        int counter = 1;
        for(int i = startIndex ; i < endIndex ; i++)
        {
            columnNames[counter] = "";
            for(int j = 3;j < inputInv[i].split(" ").length;j++){
                columnNames[counter] = columnNames[counter] + inputInv[i].split(" ")[j] + " ";
            }
            counter++;
        }

        for(int i = 0; i < rowCount;i++)
        {
            dataSmall[i][0] = data[i][0];
            for(int j = 1;j < endIndex - startIndex + 1; j++ )
            {
                dataSmall[i][j] = data[i][startIndex + j];
            }
        }
        tModel1.update(columnNames, dataSmall);
        
        
    }

    public void readInData()
    {
        int DBcolCount = 0;
        
        FileInputStream fstream;
        DataInputStream in;
        BufferedReader br;
        String[] graphDataLine;
        String url = "jdbc:mysql://grid.uhd.edu/graffiti";
        Connection con = null;
        try {
            String driver = "com.mysql.jdbc.Driver";
            Class.forName(driver).newInstance();
        }
        catch(Exception E1) {
            System.out.println("Failed to load MySQL Driver.");
            E1.printStackTrace();
            return;
        }

        try {
            con = DriverManager.getConnection(url,"graffitiguest","UHDpass");
            Statement select = con.createStatement();
            ResultSet result = select.executeQuery("select * from tempInv");
            DBcolCount = result.getMetaData().getColumnCount();

            DBcols = new String[DBcolCount+2];
            DBcols[0] = "Select Attribute";
            DBcols[1] = "Number";
            
            result = select.executeQuery("describe tempInv");
            int resNum = 2;
            while(result.next()){
                DBcols[resNum] = result.getString(1);
                resNum++;
            }
            

            for(int i = 0;i<12;i++) {
                boxModel[i] = new MyComboBoxModel();
                opModel[i] = new opBoxModel();
            }
            
            /*
            boxModel1.setSelectedItem(DBcols[0]);
            boxModel2.setSelectedItem(DBcols[0]);
            boxModel3.setSelectedItem(DBcols[0]);
            boxModel4.setSelectedItem(DBcols[0]);
            boxModel5.setSelectedItem(DBcols[0]);
            boxModel6.setSelectedItem(DBcols[0]);
            boxModel7.setSelectedItem(DBcols[0]);
            boxModel8.setSelectedItem(DBcols[0]);
            boxModel9.setSelectedItem(DBcols[0]);
            boxModel10.setSelectedItem(DBcols[0]);
            boxModel11.setSelectedItem(DBcols[0]);
            boxModel12.setSelectedItem(DBcols[0]);*/
        }
        catch(Exception E){
            E.printStackTrace();
        }

        //~~~~~~~~ File read in ~~~~~~~~~~
        try
        {
            fstream = new FileInputStream("mcm1.dat");
            in = new DataInputStream(fstream);
            br = new BufferedReader(new InputStreamReader(in));

            //reading in N
            colCount = Integer.parseInt(br.readLine().split(" ")[0]);

            inputInv = new String[colCount];

            for(int i = 0; i < colCount; i++)
            {
                inputInv[i] = br.readLine();
            }

            rowCount = Integer.parseInt(br.readLine());

            graphDataLine = new String[colCount];
            data = new String[rowCount][colCount];

            for(int i=0; i < rowCount; i++)
            {
                data[i][0] = br.readLine();
                graphDataLine = br.readLine().split(" ");

                for(int j=1; j < colCount ; j++)//
                {
                    data[i][j] = graphDataLine[j-1];
                }
            }
            fstream.close();
        }
        catch (Exception e2){
            System.err.println(//"Rows: " + rowCount + " Columns: " + colCount +"\n" +
            "\n Error: " + e2.getMessage());
        }

    }


    public void readInInv() {
        /*Reads in the Invariant lists, a shorter version of what is in the
         * MCM files that has one entry per section. This data is used
         * to populate the tree from which the invariants are selected to be put
         * into the list to be put into the driver file.
         */
        FileInputStream fstream;
        DataInputStream in;
        BufferedReader br;

        try
        {
            //TODO: alert user to missing file
            fstream = new FileInputStream("invariantListing.dat");
            in = new DataInputStream(fstream);
            br = new BufferedReader(new InputStreamReader(in));

            invCount = Integer.parseInt(br.readLine());
            inputInv = new String[invCount];

            for(int i = 0; i < invCount; i++)
            {
                inputInv[i] = br.readLine();
            }
            fstream.close();
        }
        catch (Exception e2)
        {//Catch exception if any
            System.err.println(//"Rows: " + rowCount + " Columns: " + colCount +"\n" +
            "\n Error: " + e2.getMessage());
        }

    }

    public void convertToArray(String graphString)
    {
        //places the string from the list into a 2D array of vectors.
        int graphNumber = -1;
        int catNumber = -1;
        int invNumber = -1;
        String invName = "RONG";
        String graphName = "RONG";
        //handle custom graphs
        if(graphString.split(" ")[0].contains("Custom"))
        {
            graphNumber = Integer.parseInt(graphString.split(" ")[2]);
            catNumber = Integer.parseInt(graphString.split(" ")[3]);
            invNumber = Integer.parseInt(graphString.split(" ")[4]);
        }
        else
        {   //graphs from tree
            invName = graphString.split("::")[0];
            graphName = graphString.split("::")[1];

            for(int i = 0; i < graphTypeTitle.length;i++)
            {//finding what graph we are using
                if(graphTypeTitle[i].contains(graphName))//I think this is better then ==
                {
                    graphNumber = i;
                }
            }

            for(int i = 0; i < inputInv.length;i++)
            {
                if(inputInv[i].contains(invName))
                {
                    catNumber = Integer.parseInt(inputInv[i].split(" ")[0]);//changed to 0 for new data file
                    invNumber = Integer.parseInt(inputInv[i].split(" ")[1]);//changed to 1 for new data file
                    break;
                }
            }
        }

        if( graphNumber != -1 && catNumber != -1 && invNumber != -1)
        {
            if(!(arrayG[graphNumber][catNumber - 1].contains(invNumber)))
                arrayG[graphNumber][catNumber - 1].add(invNumber);
        }
        else
        {
            System.out.println("Error: " + graphString + ": " + graphNumber + " " + catNumber + " " + invNumber);
        }
    }

    public void initializeVectorArray()
    {
        //This just populates a 2D array of vectors
        for(int i = 0; i < graphTypeTitle.length;i++)
        {
            for(int j = 0; j < invCatTitle.length; j++)
                arrayG[i][j] = new Vector();
        }
    }

    public void writeDriver()
    {
        //Creates the driver file for Dr. Delavinas attribute finder program
        DataOutputStream out;
        int sizeCounter = 0;
        //fstream = new FileInputStream("mcm1.dat");
        try
        {
            out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("Driver.dat")));

            out.writeChars(Integer.toString(graphTypeTitle.length)+ "\n");// number of graphs
            //System.out.println(graphTypeTitle.length + " //number of graphs");

            for(int i=0;i<graphTypeTitle.length;i++)
            {
                out.writeChars(Integer.toString(i) + " // graph number\n");// graph number
                //System.out.println(i + " // graph number");
                sizeCounter = 0;
                for(int j = 0; j < invCatTitle.length;j++)
                {
                    if(arrayG[i][j].size() > 0)
                    {
                        sizeCounter++;
                    }
                }
                out.writeChars(Integer.toString(sizeCounter) + " // number of nonempty categories\n"); // number of nonempty categories for graph i
             
                for(int j=0;j<invCatTitle.length;j++)
                {//System.out.println("Cat: " + j + " of " + invCatTitle.length);
                    if(arrayG[i][j].size()> 0)
                    {
                        //Below, j+1 is used because there are 6 categories, 1-7, but they are stored in an array with indicies 0-6
                        out.writeChars(Integer.toString(j+1) + " //cat number\n"); //print cat number
                        out.writeChars(Integer.toString(arrayG[i][j].size()) + " //number of invs\n");//print number of invs
                    }

                    for(int k=0;k<arrayG[i][j].size();k++)
                    {
                        out.writeChars(arrayG[i][j].get(k).toString() + "\n");//printing inv
                    }
                }
            }
            out.close();
        }
        catch (Exception e3)
        {//Catch exception if any
            System.out.println("4: " + graphTypeTitle.length + ", 7: " + invCatTitle.length);
            System.err.println("Error" + e3.getMessage());
        }
    }

//~~~~~~~~~~~~~End of Jeremy's work~~~~~~~~~~~~~~~~~~~

//Declare Custom Classes and Datatypes
    public class VisualVertex
    {
        int diam;
        String color;
        Point center;
        Point drawing;//upper left corner

        public VisualVertex() {
            center = new Point();
            center.x = 0;
            center.y = 0;

            drawing = new Point();
            drawing.x = 0;
            drawing.y = 0;

            diam = 6;
            color = "Black";
        }

        public void SetDiam(int d) {//sets diameter (adds 1 if not even) & and adjust draw point
            if (d % 2 == 1) {
                d += 1;
            }
            diam = d;
            drawing.x = center.x - diam / 2;
            drawing.y = center.y - diam / 2;
        }

        public void SetCenter(int c_x, int c_y) {//sets center and adjusts draw point wrt new center and diam
            center.x = c_x;
            center.y = c_y;
            drawing.x = c_x - diam / 2;
            drawing.y = c_y - diam / 2;
        }

        public void SetCenter(Point c) {//sets center and adjusts draw point wrt new center and diam
            center.x = c.x;
            center.y = c.y;
            drawing.x = c.x - diam / 2;
            drawing.y = c.y - diam / 2;
        }

        public void SetDrawPoint(int d_x, int d_y) {//sets draw point and adjusts center wrt new draw point and diam

            drawing.x = d_x;
            drawing.y = d_y;
            center.x = d_x + diam / 2;
            center.y = d_y + diam / 2;
        }

        public void SetDrawPoint(Point d) {//sets draw point and adjusts center wrt new draw point and diam

            drawing.x = d.x;
            drawing.y = d.y;
            center.x = d.x + diam / 2;
            center.y = d.y + diam / 2;
        }

        public void SetColor(String c) {
            color = c;
        }

        public int GetDiam() {
            return diam;
        }

        public Point GetCenter() {
            return center;
        }

        public Point GetDrawing() {
            return drawing;
        }

        public String GetColor() {
            return color;
        }

        public void Draw(Graphics g) {
            g.setColor(StringToColor(color));
            g.fillOval(drawing.x, drawing.y, diam, diam);
        }
    }

    public class VisualEdge
    {
        int a;
        int b;
        int width;
        String color;

        public VisualEdge()
        {
            a = 0;
            b = 0;
            width = 2;
            color = "Black";
        }

        public void SetA(int p)
        {
            a = p;
        }

        public void SetB(int p)
        {
            b = p;
        }

        public void SetWidth(int w)
        {
            width = w;
        }

        public void SetColor(String c)
        {
            color = c;
        }

        public int GetA() {
            return a;
        }

        public int GetB() {
            return b;
        }

        public int GetWidth() {
            return width;
        }

        public String GetColor() {
            return color;
        }

        public void Draw(Graphics g) {
            Polygon p = new Polygon();
            VisualVertex tempv; //make temp vertex
            tempv = (VisualVertex) vertices.get(a); //get first vertex
            Point va = tempv.GetCenter(); //get center
            Point vb;
            if (b != -2) //-2 is mouse location
            {
                tempv = (VisualVertex) vertices.get(b); //get second vertex
                vb = tempv.GetCenter();//get center
            } else {
                vb = mainGraphPanel.getMousePosition();
            }

            //NOTE: Order of points add to polygon matters
            if (va.x == vb.x) {//vertical line
                p.addPoint(va.x - width / 2, va.y);
                p.addPoint(va.x + width / 2, va.y);
                p.addPoint(vb.x + width / 2, vb.y);
                p.addPoint(vb.x - width / 2, vb.y);

            } else if (va.y == vb.y) {//horizontal line
                p.addPoint(va.x, va.y - width / 2);
                p.addPoint(va.x, va.y + width / 2);
                p.addPoint(vb.x, vb.y + width / 2);
                p.addPoint(vb.x, vb.y - width / 2);

            } else {
                float m = (float) (va.y - vb.y) / (float) (va.x - vb.x); //slope of line
                m = -1 / m; //slope of perpendicular line
                double constanta = va.y - va.x * m;
                double constantb = vb.y - vb.x * m;

                double x_offset = ((float) (width) / 2.0) / Math.sqrt(Math.pow(m, 2) + 1.0);
                p.addPoint((int) (va.x + x_offset), (int) ((va.x + x_offset) * m + constanta));
                p.addPoint((int) (va.x - x_offset), (int) ((va.x - x_offset) * m + constanta));
                p.addPoint((int) (vb.x - x_offset), (int) ((vb.x - x_offset) * m + constantb));
                p.addPoint((int) (vb.x + x_offset), (int) ((vb.x + x_offset) * m + constantb));

            }

            g.setColor(StringToColor(color));
            g.fillPolygon(p);
        }
    }
    //Functions

    public int GetGraphSize(String g6) {
        int size = 0;
        if (g6.getBytes()[0] < 126) {
            size = g6.getBytes()[0] - 63;
        } else if (g6.getBytes()[1] < 126) {
            size = (g6.getBytes()[1] - 63) | size;
            for (int i = 2; i < 4; i++) {
                size <<= 6;
                size = (g6.getBytes()[i] - 63) | size;
            }

        } else {
            size = (g6.getBytes()[2] - 63) | size;
            for (int i = 3; i < 8; i++) {
                size = (g6.getBytes()[i] - 63) | size;
                size <<= 6;
            }
        }

        return size;
    }

    public void GetMatrix(int size, String g6str, int[][] matrix) {
        int byteindex = 0;
        int bit = 32;
        int test;

        if (size < 63) {
            byteindex = 1;
        } else if (size < 258048) {
            byteindex = 4;
        } else {
            byteindex = 8;
        }


        char[] g6 = g6str.toCharArray();
        g6[byteindex] -= 63;
        for (int j = 0; j < size; j++) {
            for (int i = 0; i < j; i++) {
                test = bit & g6[byteindex];
                if (test == bit) {
                    matrix[i][j] = 1;
                    matrix[j][i] = 1;
                } else {
                    matrix[i][j] = 0;
                    matrix[j][i] = 0;
                }
                bit >>= 1;
                if (bit == 0) {
                    bit = 32;
                    byteindex++;

                    if (byteindex < g6.length) {
                        g6[byteindex] -= 63;
                    }
                }
            }
        }
    }

    public String GetG6(int[][] matrix) {
        int size = matrix.length;

        String g6 = "";
        char tmpch;
        byte tmpbyte;
        byte fixer = (byte) ('?');

        if (size < 63) {
            g6 += (char) (size + 63);
        } else if (size < 258047) {
            g6 += (char) 126;


            for (int i = 12; i >= 0; i -= 6) {
                tmpbyte = (byte) (size >> i);
                g6 += (char) ((tmpbyte & fixer) + 63);
                //System.out.println(g6 + " " + tmpbyte + " " + (char)((tmpbyte & fixer)+63));
            }

        } else {
            g6 += (char) 126;
            g6 += (char) 126;
            for (int i = 30; i >= 0; i -= 6) {
                tmpbyte = (byte) (size >> i);
                g6 += (char) ((tmpbyte & fixer) + 63);
            }
        }
        //ADD CASES FOR LARGER GRAPHS

        int chunk = 0;
        int bit = 0;

        for (int column = 1; column < size; column++) {
            for (int row = 0; row < column; row++) {
                if (matrix[row][column] == 1) {
                    chunk |= 1;
                }
                bit++;
                //System.out.println(matrix[row][column] + " " + (int)chunk + " " + bit);
                if (bit == 6) {
                    bit = 0;
                    g6 += (char) (chunk + 63);
                    chunk = 0;
                } else {
                    chunk <<= 1;
                }
            }
        }

        if (bit > 0) {
            chunk <<= 6 - bit - 1;
            g6 += (char) (chunk + 63);
            //System.out.println("finishing: " + (int)chunk + " " + bit);

        }

        return g6;
    }

    public void UpdateMatrix() {
        int size = vertices.size();
        if (vertices.size() == 0) {
            return;
        }
        matrix = new int[vertices.size()][vertices.size()];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = 0;
            }
        }

        VisualEdge e;
        for (int i = 0; i < edges.size(); i++) {
            e = (VisualEdge) edges.get(i);
            matrix[e.GetA()][e.GetB()] = 1;
            matrix[e.GetB()][e.GetA()] = 1;
        }
    }

    public String GetEdgeList(int size, int[][] matrix) {
        String edgeList = "";
        VisualEdge temp = new VisualEdge();
        edges.clear();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < i; j++) {
                if (matrix[i][j] == 1) {
                    temp.SetA(i);
                    temp.SetB(j);
                    temp.SetWidth(edge_width);
                    temp.SetColor((String) edgeComboBox.getSelectedItem());
                    edges.add(temp);
                    temp = new VisualEdge();
                    edgeList += "" + i + " - " + j + "\n";
                }
            }
        }

        return edgeList;
    }

    //display related
    public void RefreshVertexListForPanel(JPanel p) {
        int size = matrix.length;

        if (size == 0) {
            return;
        }

        int g_rad; //graph visual radius

        Dimension pDim = p.getSize();
        if (pDim.height > pDim.width) {
            g_rad = pDim.width / 2 - 10;
        } else {
            g_rad = pDim.height / 2 - 10;
        }
        double angle = 360 / size;

        // Get the drawing area
        int dy = p.getSize().height;
        int dx = p.getSize().width;

        //midpoint of drawing area
        int mid_y = dy / 2;
        int mid_x = dx / 2;

        //empty vertex
        VisualVertex temp = new VisualVertex();

        //clear vertices
        vertices.clear();

        //draw nodes
        for (int i = 0; i < size; i++) {
            temp.SetCenter((int) (mid_x + g_rad * Math.cos(Math.toRadians(i * angle))),
                    (int) (mid_y + g_rad * Math.sin(Math.toRadians(i * angle))));
            temp.SetDiam(vertex_diam);
            temp.SetColor((String) vertexComboBox.getSelectedItem());
            vertices.add(temp);
            temp = new VisualVertex();
        }

    }

    public void DrawFromG6(String g6, JPanel p) {
        int size = GetGraphSize(g6);
        matrix = new int[size][size];
        GetMatrix(size, g6, matrix);
        RefreshVertexListForPanel(mainGraphPanel);
        GetEdgeList(size, matrix);
        UpdateEdgeListDisplay();
        DrawGraph(mainGraphPanel.getGraphics());
    }

    public void DrawGraph(Graphics g) {
        mainGraphPanel.removeAll();
        mainGraphPanel.paintAll(g);
        VisualEdge tmpE;

        for (int i = 0; i < edges.size(); i++) {
            tmpE = (VisualEdge) edges.get(i);
            tmpE.Draw(g);
        }
        //draw highlighted edges ?

        Font font = new Font("Serif", Font.PLAIN, 20);
        g.setFont(font);

        VisualVertex tmpV;
        if (selected_index != -1) {
            for (int i = 0; i < vertices.size(); i++) {
                tmpV = (VisualVertex) vertices.get(i);
                tmpV.Draw(g);
                //for text
                g.setColor(Color.BLACK);
                String label = Integer.toString(i);
                g.drawString(label, tmpV.GetDrawing().x, tmpV.GetDrawing().y);
            }

            tmpV = (VisualVertex) vertices.get(selected_index);
            g.setColor(Color.DARK_GRAY);
            g.fillOval(tmpV.GetDrawing().x - 2, tmpV.GetDrawing().y - 2, tmpV.GetDiam() + 4, tmpV.GetDiam() + 4);
            tmpV.Draw(g);
            //for text
            g.setColor(Color.BLACK);
            String label = Integer.toString(selected_index);
            g.drawString(label, tmpV.GetDrawing().x, tmpV.GetDrawing().y);
        } else {
            for (int i = 0; i < vertices.size(); i++) {
                tmpV = (VisualVertex) vertices.get(i);
                tmpV.Draw(g);
                //for text
                g.setColor(Color.BLACK);
                String label = Integer.toString(i);
                g.drawString(label, tmpV.GetDrawing().x, tmpV.GetDrawing().y);
            }
        }
    }

    public void UpdateEdgeListDisplay() {
        VisualEdge tmp;
        DefaultListModel model = new DefaultListModel();
        String text;
        for (int i = 0; i < edges.size(); i++) {
            text = "";
            tmp = (VisualEdge) edges.get(i);
            text += tmp.GetA() + " - " + tmp.GetB();
            model.add(i, text);
        }
        edgeList.setModel(model);
    }

    public String EdgeListToString() {
        String edgeList = "";

        VisualEdge tmp;

        for (int i = 0; i < edges.size(); i++) {
            tmp = (VisualEdge) edges.get(i);
            edgeList += tmp.GetA() + " - " + tmp.GetB() + "\r\n";
        }

        return edgeList;
    }

    //Colors
    public Color StringToColor(String name) {


        if (name.equals("Blue")) {
            return Color.BLUE;
        }
        if (name.equals("Red")) {
            return Color.RED;
        }
        if (name.equals("Green")) {
            return Color.GREEN;
        }
        if (name.equals("Yellow")) {
            return Color.YELLOW;
        }
        if (name.equals("Magenta")) {
            return Color.MAGENTA;
        }

        return Color.BLACK;
    }

    //location aids
    public int VertexInLocation(Point p) {
        VisualVertex temp;
        for (int i = 0; i < vertices.size(); i++) {
            temp = (VisualVertex) vertices.get(i);
            if (p.x <= temp.GetDrawing().x + temp.GetDiam()
                    && p.x >= temp.GetDrawing().x
                    && p.y <= temp.GetDrawing().y + temp.GetDiam()
                    && p.y >= temp.GetDrawing().y) {
                return i;
            }
        }

        return -1;
    }
    //Declare Global Variables
    int vertex_diam;
    int edge_width;
    int selected_index; //used for dragging vertices
    List vertices;
    List edges;
    int[][] matrix; //stores edges with apropriate color code index
    String[] colorset;

    /** Creates new form G6ViewPlus */
    public G6ViewPlus(String[] args) {
        readInData();//added by JK
        initComponents();

        //initializations
        vertex_diam = 8;
        edge_width = 5;
        selected_index = -1;
        vertices = new ArrayList();
        edges = new ArrayList();
        matrix = new int[0][0];

        //initialize available set of colors
        colorset = new String[6];
        for (int i = 0; i < 6; i++) {
            colorset[i] = new String();
        }
        colorset[0] = "Black";
        colorset[1] = "Blue";
        colorset[2] = "Red";
        colorset[3] = "Green";
        colorset[4] = "Yellow";
        colorset[5] = "Magenta";

        //values for color comboboxes
        vertexComboBox.setModel(new javax.swing.DefaultComboBoxModel(colorset));
        edgeComboBox.setModel(new javax.swing.DefaultComboBoxModel(colorset)); //reinitialize combobox
        vertexComboBox.setEditable(false); //uneditable
        edgeComboBox.setEditable(false); //uneditable

        //clean jTextField1
        graphNameTextField.setText("");

        //clean jList1
        edgeList.setModel(new javax.swing.DefaultListModel());

        if (args.length != 0) {
            graphNameTextField.setText(args[0]);
            DrawFromG6(args[0], mainGraphPanel);
        }

    }

    public G6ViewPlus() {
        readInData();//added by JK
        initComponents();

        //initializations
        vertex_diam = 8;
        edge_width = 5;
        selected_index = -1;
        vertices = new ArrayList();
        edges = new ArrayList();
        matrix = new int[0][0];

        //initialize available set of colors
        colorset = new String[6];
        for (int i = 0; i < 6; i++) {
            colorset[i] = new String();
        }
        colorset[0] = "Black";
        colorset[1] = "Blue";
        colorset[2] = "Red";
        colorset[3] = "Green";
        colorset[4] = "Yellow";
        colorset[5] = "Magenta";

        //values for color comboboxes
        vertexComboBox.setModel(new javax.swing.DefaultComboBoxModel(colorset));
        edgeComboBox.setModel(new javax.swing.DefaultComboBoxModel(colorset)); //reinitialize combobox
        vertexComboBox.setEditable(false); //uneditable
        edgeComboBox.setEditable(false); //uneditable

        //clean jTextField1
        graphNameTextField.setText("");

        //clean jList1
        edgeList.setModel(new javax.swing.DefaultListModel());


    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFrame1 = new javax.swing.JFrame();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        tab1Panel = new javax.swing.JPanel();
        edgeListScrollPane = new javax.swing.JScrollPane();
        edgeList = new javax.swing.JList();
        mainGraphPanel = new javax.swing.JPanel();
        insertButton = new javax.swing.JToggleButton();
        vertexMenuPanel = new javax.swing.JPanel();
        setVertexButton = new javax.swing.JButton();
        deleteVertexButton = new javax.swing.JToggleButton();
        vertexComboBox = new javax.swing.JComboBox();
        vertexSizeSlider = new javax.swing.JSlider();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        saveGraphButton = new javax.swing.JButton();
        edgeMenuPanel = new javax.swing.JPanel();
        setEdgeButton = new javax.swing.JButton();
        deleteEdgeButton = new javax.swing.JToggleButton();
        edgeComboBox = new javax.swing.JComboBox();
        edgeSizeSlider = new javax.swing.JSlider();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        highlightingButton = new javax.swing.JToggleButton();
        graphGoButton = new javax.swing.JButton();
        graphNameTextField = new javax.swing.JTextField();
        jButton8 = new javax.swing.JButton();
        tab2Panel = new javax.swing.JPanel();
        invariantTreeScrollPane = new javax.swing.JScrollPane();
        readInInv();
        invariantTree = new javax.swing.JTree();
        writeDriverButton = new javax.swing.JButton();
        resetListButton = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        addInvariantButton = new javax.swing.JButton();
        invariantListScrollPane = new javax.swing.JScrollPane();
        invariantList = new javax.swing.JList(graphListModel);
        graphNumberTextField = new javax.swing.JTextField();
        categoryNumberTextField = new javax.swing.JTextField();
        invariantNumberTextField = new javax.swing.JTextField();
        tab3Panel = new javax.swing.JPanel();
        refreshInvariantsButton = new javax.swing.JButton();
        startColumnTextField = new javax.swing.JTextField();
        invariantTableScrollPane = new javax.swing.JScrollPane();
        invariantTable = new javax.swing.JTable();
        endColumnTextField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        next10Button = new javax.swing.JButton();
        last10Button = new javax.swing.JButton();
        tab5Panel = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        attributePanel = new javax.swing.JPanel();
        attNumField8 = new javax.swing.JTextField();
        OpCombo4 = new javax.swing.JComboBox();
        attNumField4 = new javax.swing.JTextField();
        attributeCombo9 = new javax.swing.JComboBox();
        attNumField10 = new javax.swing.JTextField();
        attNumField9 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        OpCombo7 = new javax.swing.JComboBox();
        OpCombo11 = new javax.swing.JComboBox();
        attNumField3 = new javax.swing.JTextField();
        attNumField1 = new javax.swing.JTextField();
        attNumField5 = new javax.swing.JTextField();
        attributeCombo5 = new javax.swing.JComboBox();
        attributeCombo3 = new javax.swing.JComboBox();
        attNumField2 = new javax.swing.JTextField();
        attributeCombo4 = new javax.swing.JComboBox();
        OpCombo5 = new javax.swing.JComboBox();
        middleBox = new javax.swing.JComboBox();
        attNumField7 = new javax.swing.JTextField();
        OpCombo6 = new javax.swing.JComboBox();
        attributeCombo2 = new javax.swing.JComboBox();
        OpCombo12 = new javax.swing.JComboBox();
        OpCombo2 = new javax.swing.JComboBox();
        OpCombo8 = new javax.swing.JComboBox();
        attNumField11 = new javax.swing.JTextField();
        attributeCombo10 = new javax.swing.JComboBox();
        attributeCombo6 = new javax.swing.JComboBox();
        attNumField6 = new javax.swing.JTextField();
        attNumField12 = new javax.swing.JTextField();
        attributeCombo11 = new javax.swing.JComboBox();
        attributeCombo1 = new javax.swing.JComboBox();
        OpCombo3 = new javax.swing.JComboBox();
        OpCombo9 = new javax.swing.JComboBox();
        attributeCombo7 = new javax.swing.JComboBox();
        attributeCombo8 = new javax.swing.JComboBox();
        attributeCombo12 = new javax.swing.JComboBox();
        OpCombo1 = new javax.swing.JComboBox();
        OpCombo10 = new javax.swing.JComboBox();
        filePanel = new javax.swing.JPanel();
        jButton14 = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jButton12 = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        jButton13 = new javax.swing.JButton();
        optionsPanel = new javax.swing.JPanel();
        jTextField7 = new javax.swing.JTextField();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel13 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jCheckBox3 = new javax.swing.JCheckBox();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();

        javax.swing.GroupLayout jFrame1Layout = new javax.swing.GroupLayout(jFrame1.getContentPane());
        jFrame1.getContentPane().setLayout(jFrame1Layout);
        jFrame1Layout.setHorizontalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame1Layout.setVerticalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentMoved(java.awt.event.ComponentEvent evt) {
                formComponentMoved(evt);
            }
        });

        edgeList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        edgeList.setFocusCycleRoot(true);
        edgeList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                edgeListMouseClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                edgeListMouseReleased(evt);
            }
        });
        edgeListScrollPane.setViewportView(edgeList);

        mainGraphPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        mainGraphPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                mainGraphPanelMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                mainGraphPanelMouseReleased(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mainGraphPanelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                mainGraphPanelMouseEntered(evt);
            }
        });
        mainGraphPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                mainGraphPanelComponentResized(evt);
            }
        });
        mainGraphPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                mainGraphPanelMouseMoved(evt);
            }
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                mainGraphPanelMouseDragged(evt);
            }
        });

        javax.swing.GroupLayout mainGraphPanelLayout = new javax.swing.GroupLayout(mainGraphPanel);
        mainGraphPanel.setLayout(mainGraphPanelLayout);
        mainGraphPanelLayout.setHorizontalGroup(
            mainGraphPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 518, Short.MAX_VALUE)
        );
        mainGraphPanelLayout.setVerticalGroup(
            mainGraphPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 583, Short.MAX_VALUE)
        );

        insertButton.setText("Insert");
        insertButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                insertButtonStateChanged(evt);
            }
        });
        insertButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertButtonActionPerformed(evt);
            }
        });

        vertexMenuPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Vertex"));

        setVertexButton.setText("Set All");
        setVertexButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        setVertexButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setVertexButtonActionPerformed(evt);
            }
        });

        deleteVertexButton.setText("Delete Mode");
        deleteVertexButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        deleteVertexButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteVertexButtonActionPerformed(evt);
            }
        });

        vertexComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        vertexSizeSlider.setMajorTickSpacing(10);
        vertexSizeSlider.setMaximum(60);
        vertexSizeSlider.setMinimum(10);
        vertexSizeSlider.setMinorTickSpacing(5);
        vertexSizeSlider.setPaintLabels(true);
        vertexSizeSlider.setPaintTicks(true);
        vertexSizeSlider.setSnapToTicks(true);
        vertexSizeSlider.setValue(10);
        vertexSizeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                vertexSizeSliderStateChanged(evt);
            }
        });

        jLabel1.setText("Size");

        jLabel2.setText("Color");

        javax.swing.GroupLayout vertexMenuPanelLayout = new javax.swing.GroupLayout(vertexMenuPanel);
        vertexMenuPanel.setLayout(vertexMenuPanelLayout);
        vertexMenuPanelLayout.setHorizontalGroup(
            vertexMenuPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(vertexMenuPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(vertexMenuPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(vertexComboBox, 0, 109, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(vertexSizeSlider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(setVertexButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
                    .addComponent(deleteVertexButton, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE))
                .addContainerGap())
        );
        vertexMenuPanelLayout.setVerticalGroup(
            vertexMenuPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(vertexMenuPanelLayout.createSequentialGroup()
                .addComponent(jLabel2)
                .addGap(5, 5, 5)
                .addComponent(vertexComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deleteVertexButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(setVertexButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(vertexSizeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        saveGraphButton.setText("Save");
        saveGraphButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveGraphButtonActionPerformed(evt);
            }
        });

        edgeMenuPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Edge"));

        setEdgeButton.setText("Set All");
        setEdgeButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        setEdgeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setEdgeButtonActionPerformed(evt);
            }
        });

        deleteEdgeButton.setText("Delete Mode");
        deleteEdgeButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        deleteEdgeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteEdgeButtonActionPerformed(evt);
            }
        });

        edgeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        edgeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                edgeComboBoxActionPerformed(evt);
            }
        });

        edgeSizeSlider.setMajorTickSpacing(10);
        edgeSizeSlider.setMaximum(35);
        edgeSizeSlider.setMinimum(5);
        edgeSizeSlider.setMinorTickSpacing(5);
        edgeSizeSlider.setPaintLabels(true);
        edgeSizeSlider.setPaintTicks(true);
        edgeSizeSlider.setSnapToTicks(true);
        edgeSizeSlider.setValue(5);
        edgeSizeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                edgeSizeSliderStateChanged(evt);
            }
        });

        jLabel3.setText("Color");

        jLabel4.setText("Size");

        javax.swing.GroupLayout edgeMenuPanelLayout = new javax.swing.GroupLayout(edgeMenuPanel);
        edgeMenuPanel.setLayout(edgeMenuPanelLayout);
        edgeMenuPanelLayout.setHorizontalGroup(
            edgeMenuPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(edgeMenuPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(edgeMenuPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(edgeComboBox, 0, 109, Short.MAX_VALUE)
                    .addComponent(deleteEdgeButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
                    .addComponent(setEdgeButton, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(edgeSizeSlider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        edgeMenuPanelLayout.setVerticalGroup(
            edgeMenuPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(edgeMenuPanelLayout.createSequentialGroup()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(edgeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deleteEdgeButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(setEdgeButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(edgeSizeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        highlightingButton.setText("Highlighting");
        highlightingButton.setToolTipText("When highligting is activated, the edit functionalities (if in Edit mode), will be disabled.");
        highlightingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                highlightingButtonActionPerformed(evt);
            }
        });

        graphGoButton.setText("Go");
        graphGoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                graphGoButtonActionPerformed(evt);
            }
        });

        graphNameTextField.setText("jTextField1");
        graphNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                graphNameTextFieldActionPerformed(evt);
            }
        });

        jButton8.setText("Reset");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout tab1PanelLayout = new javax.swing.GroupLayout(tab1Panel);
        tab1Panel.setLayout(tab1PanelLayout);
        tab1PanelLayout.setHorizontalGroup(
            tab1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tab1PanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tab1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(tab1PanelLayout.createSequentialGroup()
                        .addComponent(edgeListScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(mainGraphPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(graphNameTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 614, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tab1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(highlightingButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vertexMenuPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(edgeMenuPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, tab1PanelLayout.createSequentialGroup()
                        .addGroup(tab1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(graphGoButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(saveGraphButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(tab1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(insertButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );

        tab1PanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {graphGoButton, insertButton, jButton8, saveGraphButton});

        tab1PanelLayout.setVerticalGroup(
            tab1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tab1PanelLayout.createSequentialGroup()
                .addGroup(tab1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mainGraphPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(tab1PanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(highlightingButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(vertexMenuPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(edgeMenuPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(tab1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(insertButton)
                            .addComponent(saveGraphButton)))
                    .addComponent(edgeListScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(tab1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(graphNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(tab1PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton8)
                        .addComponent(graphGoButton)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("G6 Viewer", tab1Panel);

        initializeVectorArray();
        rootNode = new DefaultMutableTreeNode("All Invariants");
        int k = 0;
        int counter = 0;
        String displayName;

        graphNode = new DefaultMutableTreeNode[graphTypeTitle.length];
        invCatLeaf = new DefaultMutableTreeNode[graphTypeTitle.length][invCatTitle.length];

        for(int i = 0; i < graphTypeTitle.length; i++)
        {
            graphNode[i] = new DefaultMutableTreeNode(graphTypeTitle[i]);
            rootNode.add(graphNode[i]);
            counter = 0;

            for(int j=0; j < invCatTitle.length;j++)
            {
                invCatLeaf[i][j] = new DefaultMutableTreeNode(invCatTitle[j]);
                graphNode[i].add(invCatLeaf[i][j]);

                k = j + 1;
                while(counter < invCount && k  == Integer.parseInt(inputInv[counter].split(" ")[0]))
                {
                    //formatting the string to be displayed in the tree
                    displayName = "";//inputInv[counter].split(" ")[2];
                    for(int a = 2; a < inputInv[counter].split(" ").length; a++)
                    {
                        displayName = displayName + " " + inputInv[counter].split(" ")[a];
                    }
                    //adding the formatted string to the tree
                    invCatLeaf[i][j].add(new DefaultMutableTreeNode(displayName));
                    counter++;
                }
            }
        }
        treeModel = new DefaultTreeModel(rootNode);
        invariantTree.setModel(treeModel);
        invariantTree.setEditable(true);
        invariantTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                invariantTreeValueChanged(evt);
            }
        });
        invariantTreeScrollPane.setViewportView(invariantTree);

        writeDriverButton.setText("Write");
        writeDriverButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                writeDriverButtonActionPerformed(evt);
            }
        });

        resetListButton.setText("Reset");
        resetListButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetListButtonActionPerformed(evt);
            }
        });

        jLabel6.setText("Manual Input:");

        jLabel7.setText("Graph");

        jLabel8.setText("Category");

        jLabel9.setText("Invariant");

        addInvariantButton.setText("Add");
        addInvariantButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addInvariantButtonActionPerformed(evt);
            }
        });

        /*
        invariantList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { ""};//Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        */
        invariantListScrollPane.setViewportView(invariantList);

        invariantNumberTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                invariantNumberTextFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout tab2PanelLayout = new javax.swing.GroupLayout(tab2Panel);
        tab2Panel.setLayout(tab2PanelLayout);
        tab2PanelLayout.setHorizontalGroup(
            tab2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tab2PanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tab2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(writeDriverButton, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(invariantTreeScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 402, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tab2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(tab2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(tab2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(tab2PanelLayout.createSequentialGroup()
                                .addComponent(resetListButton, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
                            .addGroup(tab2PanelLayout.createSequentialGroup()
                                .addGap(4, 4, 4)
                                .addComponent(jLabel7)
                                .addGap(2, 2, 2)
                                .addComponent(graphNumberTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(categoryNumberTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(invariantNumberTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(32, 32, 32))
                            .addGroup(tab2PanelLayout.createSequentialGroup()
                                .addComponent(invariantListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 351, Short.MAX_VALUE)
                                .addContainerGap()))
                        .addGroup(tab2PanelLayout.createSequentialGroup()
                            .addComponent(addInvariantButton)
                            .addContainerGap()))
                    .addGroup(tab2PanelLayout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addContainerGap())))
        );

        tab2PanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addInvariantButton, resetListButton});

        tab2PanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {categoryNumberTextField, graphNumberTextField, invariantNumberTextField});

        tab2PanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel7, jLabel8, jLabel9});

        tab2PanelLayout.setVerticalGroup(
            tab2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, tab2PanelLayout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(tab2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(tab2PanelLayout.createSequentialGroup()
                        .addComponent(invariantListScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 404, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(resetListButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel6)
                        .addGap(18, 18, 18)
                        .addGroup(tab2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(categoryNumberTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7)
                            .addComponent(graphNumberTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9)
                            .addComponent(invariantNumberTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(11, 11, 11)
                        .addComponent(addInvariantButton))
                    .addComponent(invariantTreeScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 557, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(writeDriverButton)
                .addGap(14, 14, 14))
        );

        tab2PanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {addInvariantButton, resetListButton});

        jTabbedPane1.addTab("Select Invariants", tab2Panel);

        refreshInvariantsButton.setText("Refresh");
        refreshInvariantsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshInvariantsButtonActionPerformed(evt);
            }
        });

        startColumnTextField.setText("0");
        startColumnTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startColumnTextFieldActionPerformed(evt);
            }
        });

        invariantTableScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        invariantTableScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        invariantTableScrollPane.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                invariantTableScrollPaneMouseWheelMoved(evt);
            }
        });

        //readInData();

        showSubsetData(0,10);
        invariantTable.setModel(tModel1);
        invariantTableScrollPane.setViewportView(invariantTable);

        endColumnTextField.setText("10");
        endColumnTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                endColumnTextFieldActionPerformed(evt);
            }
        });

        jLabel5.setText(" -");

        next10Button.setText("Next 10");
        next10Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                next10ButtonActionPerformed(evt);
            }
        });

        last10Button.setText("Previous 10");
        last10Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                last10ButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout tab3PanelLayout = new javax.swing.GroupLayout(tab3Panel);
        tab3Panel.setLayout(tab3PanelLayout);
        tab3PanelLayout.setHorizontalGroup(
            tab3PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tab3PanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tab3PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(invariantTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 765, Short.MAX_VALUE)
                    .addGroup(tab3PanelLayout.createSequentialGroup()
                        .addComponent(refreshInvariantsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(32, 32, 32)
                        .addComponent(startColumnTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(endColumnTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(tab3PanelLayout.createSequentialGroup()
                        .addComponent(last10Button)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 585, Short.MAX_VALUE)
                        .addComponent(next10Button)))
                .addContainerGap())
        );

        tab3PanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {last10Button, next10Button, refreshInvariantsButton});

        tab3PanelLayout.setVerticalGroup(
            tab3PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tab3PanelLayout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addGroup(tab3PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(startColumnTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(endColumnTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(refreshInvariantsButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(invariantTableScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 521, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(tab3PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(last10Button)
                    .addComponent(next10Button))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tab3PanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {last10Button, next10Button, refreshInvariantsButton});

        jTabbedPane1.addTab("Inspect Invariants", tab3Panel);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        attNumField8.setEditable(false);

        OpCombo4.setModel(opModel[3]);

        attNumField4.setEditable(false);

        //System.out.println(boxModel[8].selectedObject.toString());
        attributeCombo9.setModel(boxModel[8]);
        attributeCombo9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attributeCombo9ActionPerformed(evt);
            }
        });

        attNumField10.setEditable(false);

        attNumField9.setEditable(false);

        jButton1.setText("Search for Graph");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        OpCombo7.setModel(opModel[6]);

        OpCombo11.setModel(opModel[10]);

        attNumField3.setEditable(false);

        attNumField1.setEditable(false);

        attNumField5.setEditable(false);

        attributeCombo5.setModel(boxModel[4]);
        attributeCombo5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attributeCombo5ActionPerformed(evt);
            }
        });

        attributeCombo3.setModel(boxModel[2]);
        attributeCombo3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attributeCombo3ActionPerformed(evt);
            }
        });

        attNumField2.setEditable(false);

        attributeCombo4.setModel(boxModel[3]);
        attributeCombo4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attributeCombo4ActionPerformed(evt);
            }
        });

        OpCombo5.setModel(opModel[4]);

        middleBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<", ">", "=", "<=", "=>", "!=" }));

        attNumField7.setEditable(false);

        OpCombo6.setModel(opModel[5]);

        attributeCombo2.setModel(boxModel[1]);
        attributeCombo2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attributeCombo2ActionPerformed(evt);
            }
        });

        OpCombo12.setModel(opModel[11]);

        OpCombo2.setModel(opModel[1]);

        OpCombo8.setModel(opModel[7]);

        attNumField11.setEditable(false);

        attributeCombo10.setModel(boxModel[9]);
        attributeCombo10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attributeCombo10ActionPerformed(evt);
            }
        });

        attributeCombo6.setModel(boxModel[5]);
        attributeCombo6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attributeCombo6ActionPerformed(evt);
            }
        });

        attNumField6.setEditable(false);

        attNumField12.setEditable(false);

        attributeCombo11.setModel(boxModel[10]);
        attributeCombo11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attributeCombo11ActionPerformed(evt);
            }
        });

        attributeCombo1.setModel(boxModel[0]);
        attributeCombo1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attributeCombo1ActionPerformed(evt);
            }
        });

        OpCombo3.setModel(opModel[2]);

        OpCombo9.setModel(opModel[8]);

        attributeCombo7.setModel(boxModel[6]);
        attributeCombo7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attributeCombo7ActionPerformed(evt);
            }
        });

        attributeCombo8.setModel(boxModel[7]);
        attributeCombo8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attributeCombo8ActionPerformed(evt);
            }
        });

        attributeCombo12.setModel(boxModel[11]);
        attributeCombo12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attributeCombo12ActionPerformed(evt);
            }
        });

        OpCombo1.setModel(opModel[0]);

        OpCombo10.setModel(opModel[9]);

        javax.swing.GroupLayout attributePanelLayout = new javax.swing.GroupLayout(attributePanel);
        attributePanel.setLayout(attributePanelLayout);
        attributePanelLayout.setHorizontalGroup(
            attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attributePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(attributePanelLayout.createSequentialGroup()
                        .addGap(58, 58, 58)
                        .addComponent(jButton1))
                    .addGroup(attributePanelLayout.createSequentialGroup()
                        .addGroup(attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(attributeCombo3, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(attributeCombo2, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(attNumField3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(attNumField2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(OpCombo2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(OpCombo3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(attributePanelLayout.createSequentialGroup()
                        .addGroup(attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(attributeCombo6, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(attributeCombo4, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(attributeCombo5, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(attNumField6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(attNumField4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(attNumField5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(OpCombo6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(OpCombo5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(OpCombo4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(attributePanelLayout.createSequentialGroup()
                        .addGroup(attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(attributeCombo8, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(attributeCombo9, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(attributeCombo11, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(attributeCombo10, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(attributeCombo12, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(middleBox, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(attNumField9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(attNumField12, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(attNumField11, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(attNumField10, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(attNumField8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(OpCombo9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(OpCombo10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(OpCombo12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(OpCombo11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(OpCombo8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, attributePanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(attributeCombo1, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(attNumField1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(OpCombo1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(attributePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(attributeCombo7, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(attNumField7, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(OpCombo7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        attributePanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {OpCombo1, OpCombo10, OpCombo11, OpCombo12, OpCombo2, OpCombo3, OpCombo4, OpCombo5, OpCombo6, OpCombo7, OpCombo8, OpCombo9});

        attributePanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {attNumField1, attNumField10, attNumField11, attNumField12, attNumField2, attNumField3, attNumField4, attNumField5, attNumField6, attNumField7, attNumField8, attNumField9});

        attributePanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {attributeCombo1, attributeCombo10, attributeCombo11, attributeCombo12, attributeCombo2, attributeCombo3, attributeCombo4, attributeCombo5, attributeCombo6, attributeCombo7, attributeCombo8, attributeCombo9});

        attributePanelLayout.setVerticalGroup(
            attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attributePanelLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(attributeCombo1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(attNumField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(OpCombo1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(attributeCombo2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(attNumField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(OpCombo2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(attributeCombo3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(attNumField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(OpCombo3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(attributePanelLayout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addComponent(OpCombo5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(OpCombo6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(OpCombo4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(attributePanelLayout.createSequentialGroup()
                        .addComponent(attributeCombo4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(attributeCombo5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(attNumField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(attributeCombo6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(attNumField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(attNumField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(middleBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(attributeCombo7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(attNumField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(OpCombo7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(attributeCombo8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(attNumField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(OpCombo8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(attributeCombo9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(attNumField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(OpCombo9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(attributePanelLayout.createSequentialGroup()
                        .addComponent(attributeCombo10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(attributeCombo11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(attNumField11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(attributePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(attributeCombo12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(attNumField12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(attNumField10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(attributePanelLayout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addComponent(OpCombo11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(OpCombo12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(OpCombo10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addContainerGap())
        );

        attributePanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {OpCombo1, OpCombo10, OpCombo11, OpCombo12, OpCombo2, OpCombo3, OpCombo4, OpCombo5, OpCombo6, OpCombo7, OpCombo8, OpCombo9, attNumField1, attNumField10, attNumField11, attNumField12, attNumField2, attNumField3, attNumField4, attNumField5, attNumField6, attNumField7, attNumField8, attNumField9, attributeCombo1, attributeCombo10, attributeCombo11, attributeCombo12, attributeCombo2, attributeCombo3, attributeCombo4, attributeCombo5, attributeCombo6, attributeCombo7, attributeCombo8, attributeCombo9, middleBox});

        jButton14.setText("Run Builddbs");
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton14ActionPerformed(evt);
            }
        });

        jLabel10.setText("Select Graph File");

        jButton12.setText("Find File");

        jLabel11.setText("Choose Driver File");

        jButton13.setText("Find File");

        javax.swing.GroupLayout filePanelLayout = new javax.swing.GroupLayout(filePanel);
        filePanel.setLayout(filePanelLayout);
        filePanelLayout.setHorizontalGroup(
            filePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filePanelLayout.createSequentialGroup()
                .addGroup(filePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(filePanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(filePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(filePanelLayout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addGap(18, 18, 18)
                                .addComponent(jButton13))
                            .addGroup(filePanelLayout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addGap(18, 18, 18)
                                .addComponent(jButton12))))
                    .addGroup(filePanelLayout.createSequentialGroup()
                        .addGap(70, 70, 70)
                        .addComponent(jButton14)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        filePanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel10, jLabel11});

        filePanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButton12, jButton13, jButton14});

        filePanelLayout.setVerticalGroup(
            filePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(filePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addComponent(jButton12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(filePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jButton13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 92, Short.MAX_VALUE)
                .addComponent(jButton14)
                .addContainerGap())
        );

        filePanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jButton12, jButton13, jButton14});

        filePanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jLabel10, jLabel11});

        jCheckBox2.setText("View output in Tab 3");

        jCheckBox1.setText("Write to file");

        jLabel13.setText("Choose Output File Name");

        jLabel12.setText("Options");

        jCheckBox3.setText("Append to Existing data");

        javax.swing.GroupLayout optionsPanelLayout = new javax.swing.GroupLayout(optionsPanel);
        optionsPanel.setLayout(optionsPanelLayout);
        optionsPanelLayout.setHorizontalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox1)
                    .addGroup(optionsPanelLayout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(optionsPanelLayout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel13)))
                    .addComponent(jLabel12)
                    .addComponent(jCheckBox2)
                    .addGroup(optionsPanelLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jCheckBox3)))
                .addContainerGap(55, Short.MAX_VALUE))
        );

        optionsPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jCheckBox1, jCheckBox2, jCheckBox3});

        optionsPanelLayout.setVerticalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12)
                .addGap(16, 16, 16)
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addComponent(jCheckBox2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox3))
        );

        optionsPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jCheckBox1, jCheckBox2});

        javax.swing.GroupLayout tab5PanelLayout = new javax.swing.GroupLayout(tab5Panel);
        tab5Panel.setLayout(tab5PanelLayout);
        tab5PanelLayout.setHorizontalGroup(
            tab5PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tab5PanelLayout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addGroup(tab5PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(tab5PanelLayout.createSequentialGroup()
                        .addGroup(tab5PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(filePanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(optionsPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(41, 41, 41))
                    .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE))
                .addGap(28, 28, 28)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41)
                .addComponent(attributePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(95, 95, 95))
        );
        tab5PanelLayout.setVerticalGroup(
            tab5PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tab5PanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tab5PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 541, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, tab5PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(attributePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, tab5PanelLayout.createSequentialGroup()
                            .addGap(27, 27, 27)
                            .addComponent(optionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 77, Short.MAX_VALUE)
                            .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(37, 37, 37)
                            .addComponent(filePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Run Program", tab5Panel);

        jMenu1.setText("File");

        jMenuItem1.setText("Launch Organizer");
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Tools");

        jMenuItem3.setText("Database Login");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem3);

        jMenuItem4.setText("Upload MCM file");
        jMenu2.add(jMenuItem4);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 805, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 651, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(33, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        DrawGraph(mainGraphPanel.getGraphics());
    }//GEN-LAST:event_formWindowActivated

    private void formComponentMoved(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentMoved
        DrawGraph(mainGraphPanel.getGraphics());
    }//GEN-LAST:event_formComponentMoved

    private void graphNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_graphNameTextFieldActionPerformed
        // Old stuff
    }//GEN-LAST:event_graphNameTextFieldActionPerformed

    private void graphGoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_graphGoButtonActionPerformed

        /*NODE: Removing other modes may inconvinience user*/
        //        jToggleButton1.setSelected(false); //edit mode set to off
        //        jToggleButton2.setSelected(false); //vertex delete mode set to off
        //        jToggleButton3.setSelected(false); //edge delete mode set to off
        //        jToggleButton4.setSelected(false); //edit highlight mode set to off
        String g6 = graphNameTextField.getText();
        if (!g6.isEmpty()) {
            DrawFromG6(g6, mainGraphPanel);
        }
}//GEN-LAST:event_graphGoButtonActionPerformed

    private void highlightingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_highlightingButtonActionPerformed
        if (highlightingButton.isSelected()) {
            insertButton.setSelected(false);
            deleteVertexButton.setSelected(false);
            deleteEdgeButton.setSelected(false);
        }
}//GEN-LAST:event_highlightingButtonActionPerformed

    private void edgeSizeSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_edgeSizeSliderStateChanged
        edge_width = edgeSizeSlider.getValue();
        VisualEdge temp;
        for (int i = 0; i < edges.size(); i++) {
            temp = (VisualEdge) edges.get(i);
            temp.SetWidth(edgeSizeSlider.getValue());
        }
        DrawGraph(mainGraphPanel.getGraphics());
}//GEN-LAST:event_edgeSizeSliderStateChanged

    private void edgeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_edgeComboBoxActionPerformed
        if (highlightingButton.isSelected() && !edgeList.isSelectionEmpty()) {
            VisualEdge e;
            int[] selected = edgeList.getSelectedIndices();
            for (int i = 0; i < selected.length; i++) {
                e = (VisualEdge) edges.get(selected[i]);
                e.SetColor((String) edgeComboBox.getSelectedItem());
            }
            DrawGraph(mainGraphPanel.getGraphics());
        }
}//GEN-LAST:event_edgeComboBoxActionPerformed

    private void deleteEdgeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteEdgeButtonActionPerformed
        if (deleteVertexButton.isSelected()) {
            insertButton.setSelected(false); //turn off edit mode
            highlightingButton.setSelected(false); //turn off highlight mode
        }
}//GEN-LAST:event_deleteEdgeButtonActionPerformed

    private void setEdgeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setEdgeButtonActionPerformed
        VisualEdge e;

        for (int i = 0; i < edges.size(); i++) {
            e = (VisualEdge) edges.get(i);
            e.SetColor((String) edgeComboBox.getSelectedItem());
        }

        DrawGraph(mainGraphPanel.getGraphics());
}//GEN-LAST:event_setEdgeButtonActionPerformed

    private void saveGraphButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveGraphButtonActionPerformed


        String filename = JOptionPane.showInputDialog("Please enter a name for your graph");
        File saveFile = new File(filename + ".jpg");
        //Image drawing = createImage(jPanel1.getSize().width,jPanel1.getSize().height);
        BufferedImage bi = new BufferedImage(mainGraphPanel.getSize().width, mainGraphPanel.getSize().height, BufferedImage.TYPE_INT_RGB);
        Graphics g = bi.getGraphics();
        DrawGraph(g);
        try {
            ImageIO.write(bi, "jpg", saveFile);
        } catch (IOException ex) {
            Logger.getLogger(G6ViewPlus.class.getName()).log(Level.SEVERE, null, ex);
        }
        g.dispose();



        String g6Text = graphNameTextField.getText();
        String edgeList = EdgeListToString();


        FileWriter outputStream = null;
        try {
            outputStream = new FileWriter(filename + "_g6.txt");
            outputStream.write(g6Text);
            outputStream.close();
        } catch (IOException ex) {
            Logger.getLogger(G6ViewPlus.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            outputStream = new FileWriter(filename + "_edgelist.txt");
            outputStream.write(edgeList);
            outputStream.close();
        } catch (IOException ex) {
            Logger.getLogger(G6ViewPlus.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_saveGraphButtonActionPerformed

    private void vertexSizeSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_vertexSizeSliderStateChanged
        vertex_diam = vertexSizeSlider.getValue();
        VisualVertex temp;
        for (int i = 0; i < vertices.size(); i++) {
            temp = (VisualVertex) vertices.get(i);
            temp.SetDiam(vertexSizeSlider.getValue());
        }
        DrawGraph(mainGraphPanel.getGraphics());
}//GEN-LAST:event_vertexSizeSliderStateChanged

    private void deleteVertexButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteVertexButtonActionPerformed
        if (deleteVertexButton.isSelected()) {
            insertButton.setSelected(false); //turn off edit mode
            highlightingButton.setSelected(false); //turn off highlight mode
        }
}//GEN-LAST:event_deleteVertexButtonActionPerformed

    private void setVertexButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setVertexButtonActionPerformed
        VisualVertex v;

        for (int i = 0; i < vertices.size(); i++) {
            v = (VisualVertex) vertices.get(i);
            v.SetColor((String) vertexComboBox.getSelectedItem());
        }

        DrawGraph(mainGraphPanel.getGraphics());
}//GEN-LAST:event_setVertexButtonActionPerformed

    private void insertButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertButtonActionPerformed
        if (insertButton.isSelected()) {
            highlightingButton.setSelected(false);
            deleteEdgeButton.setSelected(false);
            deleteVertexButton.setSelected(false);

        }
}//GEN-LAST:event_insertButtonActionPerformed

    private void insertButtonStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_insertButtonStateChanged
        //if(jToggleButton1.isSelected())
        //{
        UpdateMatrix();
        graphNameTextField.setText("");
        graphNameTextField.setText(GetG6(matrix));
        //}
}//GEN-LAST:event_insertButtonStateChanged

    private void mainGraphPanelMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mainGraphPanelMouseMoved
        int old = selected_index;
        selected_index = VertexInLocation(evt.getPoint());
        if (old != selected_index) {
            DrawGraph(mainGraphPanel.getGraphics());
        }
}//GEN-LAST:event_mainGraphPanelMouseMoved

    private void mainGraphPanelMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mainGraphPanelMouseDragged
        if (insertButton.isSelected() && !evt.isShiftDown()) {
            selected_index = VertexInLocation(evt.getPoint());
            DrawGraph(mainGraphPanel.getGraphics());
        } else if (selected_index != -1) {
            VisualVertex temp;
            temp = (VisualVertex) vertices.get(selected_index);
            temp.SetCenter(mainGraphPanel.getMousePosition());
            DrawGraph(mainGraphPanel.getGraphics());
        }
}//GEN-LAST:event_mainGraphPanelMouseDragged

    private void mainGraphPanelComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_mainGraphPanelComponentResized
        DrawGraph(mainGraphPanel.getGraphics());
}//GEN-LAST:event_mainGraphPanelComponentResized

    private void mainGraphPanelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mainGraphPanelMouseReleased
        if (insertButton.isSelected() && //is in edit mode
                !evt.isShiftDown() && //shift is not held
                evt.getButton() == MouseEvent.BUTTON1) //the left mouse button is released
        {
            VisualEdge e;
            e = (VisualEdge) edges.get(edges.size() - 1);

            if (e.GetB() == -2) {
                if (selected_index == -1) {
                    VisualVertex n = new VisualVertex();
                    n.SetColor((String) vertexComboBox.getSelectedItem());
                    n.SetCenter(evt.getPoint());
                    n.SetDiam(vertexSizeSlider.getValue());
                    vertices.add(n);
                    e.SetB(vertices.size() - 1);

                    selected_index = vertices.size() - 1;

                } else {
                    if ((e.GetA() != selected_index) && (matrix[e.GetA()][selected_index] != 1)) {
                        e.SetB(selected_index);
                    } else {
                        edges.remove(edges.size() - 1);
                    }
                }
            }


            // selected_index = -1;
            UpdateMatrix();
            graphNameTextField.setText("");
            graphNameTextField.setText(GetG6(matrix));
            DrawGraph(mainGraphPanel.getGraphics());
            UpdateEdgeListDisplay();
        }
}//GEN-LAST:event_mainGraphPanelMouseReleased

    private void mainGraphPanelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mainGraphPanelMousePressed
        //Click and hold: Always for moving nodes

        //this bit is redundant sincehovering selects vertex
        /*     if(selected_index == -1)
        selected_index = VertexInLocation(evt.getPoint());*/

        //edit mode is active&& shift isn't pressed, create edge
        if (insertButton.isSelected() && !evt.isShiftDown() && selected_index != -1) {
            if (evt.getButton() == MouseEvent.BUTTON1) //left?
            {
                VisualEdge n = new VisualEdge();
                n.SetA(selected_index);
                n.SetB(-2); //code for mouse location
                n.SetColor((String) edgeComboBox.getSelectedItem());
                n.SetWidth(edgeSizeSlider.getValue());
                edges.add(n);
            } else//other button
            {
            }
        }
    }//GEN-LAST:event_mainGraphPanelMousePressed

    private void mainGraphPanelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mainGraphPanelMouseEntered
        DrawGraph(mainGraphPanel.getGraphics());
}//GEN-LAST:event_mainGraphPanelMouseEntered

    private void mainGraphPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mainGraphPanelMouseClicked
        if (deleteVertexButton.isSelected()) {//vertex delete mode selected
            if (selected_index != -1) {
                //Kill all adjacent edges
                VisualEdge e;
                for (int i = 0; i < edges.size(); i++) {
                    e = (VisualEdge) edges.get(i);
                    if (e.GetA() == selected_index || e.GetB() == selected_index)//if onnected to vertex
                    {
                        edges.remove(i);
                        i--;
                    }

                }
                for (int i = 0; i < edges.size(); i++) {
                    e = (VisualEdge) edges.get(i);
                    if (e.GetA() > selected_index) //if connected to a higher verex reduce index
                    {
                        e.SetA(e.GetA() - 1);
                    }
                    if (e.GetB() > selected_index) //if connected to a higher verex reduce index
                    {
                        e.SetB(e.GetB() - 1);
                    }
                }

                vertices.remove(selected_index);
                selected_index = -1;

                UpdateMatrix();
                graphNameTextField.setText("");
                graphNameTextField.setText(GetG6(matrix));
                UpdateEdgeListDisplay();
                DrawGraph(mainGraphPanel.getGraphics());
            }

        } else if (deleteEdgeButton.isSelected()) //delete edge mode is uniquely selected
        {//vertex delete mode selected
            if (selected_index != -1) {
                //Highlight all adjacent edges to the clicked vertices
                List indices = new ArrayList();
                VisualEdge e;
                for (int i = 0; i < edges.size(); i++) {
                    e = (VisualEdge) edges.get(i);
                    if (e.GetA() == selected_index || e.GetB() == selected_index)//if onnected to vertex
                    {
                        indices.add(i);
                    }
                }
                int[] index_array = new int[indices.size()];
                for (int i = 0; i < index_array.length; i++) {
                    index_array[i] = (int) Integer.decode(indices.get(i).toString());
                }
                edgeList.setSelectedIndices(index_array);
                // DrawGraph(jPanel1.getGraphics()); //Note rendered highlighting of edges yet
            }

        }

        if (highlightingButton.isSelected()) {
            if (selected_index != -1) {
                VisualVertex v;
                v = (VisualVertex) vertices.get(selected_index);
                v.SetColor((String) vertexComboBox.getSelectedItem());
                DrawGraph(mainGraphPanel.getGraphics());
            }
        }
        if (insertButton.isSelected())//edit mode is active
        {
            if (evt.getButton() == MouseEvent.BUTTON1 && selected_index == -1) //left?
            {
                VisualVertex n = new VisualVertex();
                n.SetColor((String) vertexComboBox.getSelectedItem());
                n.SetCenter(evt.getPoint());
                n.SetDiam(vertexSizeSlider.getValue());
                vertices.add(n);

                selected_index = vertices.size() - 1;
                UpdateMatrix();
                graphNameTextField.setText("");
                graphNameTextField.setText(GetG6(matrix));
                DrawGraph(mainGraphPanel.getGraphics());
            } else//other button
            {
            }
        }
}//GEN-LAST:event_mainGraphPanelMouseClicked

    private void edgeListMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_edgeListMouseReleased
        if (!evt.isShiftDown() && !evt.isControlDown()) {
            edgeList.clearSelection();
        }
}//GEN-LAST:event_edgeListMouseReleased

    private void edgeListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_edgeListMouseClicked
        //BUG: id clicked on empty space below last edge
        //last edge is highlighted
        if (highlightingButton.isSelected() && !evt.isControlDown() && !evt.isShiftDown()) {
            VisualEdge e;
            int index = edgeList.locationToIndex(edgeList.getMousePosition());
            if (index != -1) {
                e = (VisualEdge) edges.get(index);
                e.SetColor((String) edgeComboBox.getSelectedItem());
                DrawGraph(mainGraphPanel.getGraphics());
            }
        }
        if (deleteEdgeButton.isSelected())//in delete edge mode
        {
            int index = edgeList.locationToIndex(edgeList.getMousePosition());
            if (index != -1) {
                edges.remove(index);
            }

            UpdateMatrix();
            graphNameTextField.setText("");
            graphNameTextField.setText(GetG6(matrix));
            UpdateEdgeListDisplay();
            DrawGraph(mainGraphPanel.getGraphics());

        }
}//GEN-LAST:event_edgeListMouseClicked

    private void refreshInvariantsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshInvariantsButtonActionPerformed
        //Updating data for tab 3 to desired range
        if(Integer.parseInt(startColumnTextField.getText()) < 0 ||
                Integer.parseInt(endColumnTextField.getText()) > colCount ||
                Integer.parseInt(endColumnTextField.getText()) < Integer.parseInt(startColumnTextField.getText())) {
            startColumnTextField.setText("0");
            endColumnTextField.setText("10");
        }
        showSubsetData(Integer.parseInt(startColumnTextField.getText()), Integer.parseInt(endColumnTextField.getText()));
}//GEN-LAST:event_refreshInvariantsButtonActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // deletes all edges and vertices and resets the graph.
        for (int i = edges.size() - 1; i >= 0 ; i--)
        {
            edges.remove(i);
        }

        for (int i = vertices.size() - 1; i >= 0; i--)
        {
            vertices.remove(i);
        }
            selected_index = -1;
            UpdateMatrix();
            graphNameTextField.setText("");
            graphNameTextField.setText(GetG6(matrix));
            UpdateEdgeListDisplay();
            DrawGraph(mainGraphPanel.getGraphics());
    }//GEN-LAST:event_jButton8ActionPerformed

    private void writeDriverButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_writeDriverButtonActionPerformed
        //convert strings to appropriate numbers
        for(int i=0; i < graphListModel.size();i++)
        {
            convertToArray(graphListModel.get(i).toString());
        }
        writeDriver();
    }//GEN-LAST:event_writeDriverButtonActionPerformed

    private void startColumnTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startColumnTextFieldActionPerformed
        // Not going to do anything, textfield just for entering data
    }//GEN-LAST:event_startColumnTextFieldActionPerformed

    private void endColumnTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_endColumnTextFieldActionPerformed
        // Not going to do anything, textfield just for entering data
    }//GEN-LAST:event_endColumnTextFieldActionPerformed

    private void invariantNumberTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_invariantNumberTextFieldActionPerformed
        // Not going to do anything, textfield just for entering data
    }//GEN-LAST:event_invariantNumberTextFieldActionPerformed

    private void invariantTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_invariantTreeValueChanged
                DefaultMutableTreeNode readNode = (DefaultMutableTreeNode)
                           invariantTree.getLastSelectedPathComponent();
        if (readNode == null)
            return;

        Object nodeInfo = readNode.getUserObject();
        if (readNode.isLeaf() && !graphListModel.contains(nodeInfo.toString() + "::" + readNode.getParent().getParent().toString()))
        {
            graphListModel.addElement(nodeInfo.toString() + "::" + readNode.getParent().getParent().toString());

        }
    }//GEN-LAST:event_invariantTreeValueChanged

    private void addInvariantButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addInvariantButtonActionPerformed
        if(graphNumberTextField.getText().equals("") ||
                categoryNumberTextField.getText().equals("") ||
                invariantNumberTextField.getText().equals("") ||
                Integer.parseInt(graphNumberTextField.getText()) >= 4 ||
                Integer.parseInt(categoryNumberTextField.getText()) >= 7 ) {
            System.out.println("Incorrect input");
        }
        else {
            if (graphListModel.contains("Custom Invariant: " + graphNumberTextField.getText() + " " + categoryNumberTextField.getText() + " " + invariantNumberTextField.getText())){
                System.out.println("Error: Already in the list.");
            }
            else {
                graphListModel.addElement("Custom Invariant: " + graphNumberTextField.getText() + " " + categoryNumberTextField.getText() + " " + invariantNumberTextField.getText());
            }
        }
    }//GEN-LAST:event_addInvariantButtonActionPerformed

    private void next10ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_next10ButtonActionPerformed
        //Input Checking

        if(Integer.parseInt(startColumnTextField.getText()) > Integer.parseInt(endColumnTextField.getText())){
            String switchVar = startColumnTextField.getText();
            startColumnTextField.setText(endColumnTextField.getText());
            endColumnTextField.setText(switchVar);
        }

        if(Integer.parseInt(endColumnTextField.getText()) >= colCount - 10 || Integer.parseInt(startColumnTextField.getText()) >= colCount - 10) {
            startColumnTextField.setText(Integer.toString(colCount - 11));
            endColumnTextField.setText(Integer.toString(colCount - 1));
        }
        else
            if(Integer.parseInt(startColumnTextField.getText()) < 0 || Integer.parseInt(startColumnTextField.getText()) < 0) {
            startColumnTextField.setText("0");
            endColumnTextField.setText("10");
        }
        else {
            startColumnTextField.setText(Integer.toString(Integer.parseInt(startColumnTextField.getText()) + 10));
            endColumnTextField.setText(Integer.toString(Integer.parseInt(endColumnTextField.getText()) + 10));
        }
        showSubsetData(Integer.parseInt(startColumnTextField.getText()), Integer.parseInt(endColumnTextField.getText()));

    }//GEN-LAST:event_next10ButtonActionPerformed

    private void resetListButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetListButtonActionPerformed
        graphListModel.removeAllElements();
        initializeVectorArray();
    }//GEN-LAST:event_resetListButtonActionPerformed

    private void invariantTableScrollPaneMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_invariantTableScrollPaneMouseWheelMoved
        // need to figure out how to get rid of this.
    }//GEN-LAST:event_invariantTableScrollPaneMouseWheelMoved

    private void last10ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_last10ButtonActionPerformed
        //Input Checking
              
        if(Integer.parseInt(startColumnTextField.getText()) > Integer.parseInt(endColumnTextField.getText())){
            String switchVar = startColumnTextField.getText();
            startColumnTextField.setText(endColumnTextField.getText());
            endColumnTextField.setText(switchVar);
        }
        
        if(Integer.parseInt(endColumnTextField.getText()) > colCount || Integer.parseInt(startColumnTextField.getText()) > colCount) {
            startColumnTextField.setText(Integer.toString(colCount - 11));
            endColumnTextField.setText(Integer.toString(colCount - 1));
        }
        else if(Integer.parseInt(startColumnTextField.getText()) < 10 || Integer.parseInt(startColumnTextField.getText()) < 10) {
            startColumnTextField.setText("0");
            endColumnTextField.setText("10");
        }
        else
        {
        startColumnTextField.setText(Integer.toString(Integer.parseInt(startColumnTextField.getText()) - 10));
        endColumnTextField.setText(Integer.toString(Integer.parseInt(endColumnTextField.getText()) - 10));
        }
        showSubsetData(Integer.parseInt(startColumnTextField.getText()), Integer.parseInt(endColumnTextField.getText()));
    }//GEN-LAST:event_last10ButtonActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        // TODO: open a dialog window to ask a username and password
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed
        // TODO: run the number crunching program
    }//GEN-LAST:event_jButton14ActionPerformed

    private void attributeCombo1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attributeCombo1ActionPerformed
        // TODO add your handling code here:
        if(attributeCombo1.getSelectedIndex() == 1) {
            attNumField1.setEditable(true);
        }
        else {
            attNumField1.setEditable(false);
        }
    }//GEN-LAST:event_attributeCombo1ActionPerformed

    private void attributeCombo2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attributeCombo2ActionPerformed
        if(attributeCombo2.getSelectedIndex() == 1) {
            attNumField2.setEditable(true);
        }
        else {
            attNumField2.setEditable(false);
        }
    }//GEN-LAST:event_attributeCombo2ActionPerformed

    private void attributeCombo3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attributeCombo3ActionPerformed
        if(attributeCombo3.getSelectedIndex() == 1) {
            attNumField3.setEditable(true);
        }
        else {
            attNumField3.setEditable(false);
        }
    }//GEN-LAST:event_attributeCombo3ActionPerformed

    private void attributeCombo7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attributeCombo7ActionPerformed
        if(attributeCombo7.getSelectedIndex() == 1) {
            attNumField7.setEditable(true);
        }
        else {
            attNumField7.setEditable(false);
        }
    }//GEN-LAST:event_attributeCombo7ActionPerformed

    private void attributeCombo8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attributeCombo8ActionPerformed
        if(attributeCombo8.getSelectedIndex() == 1) {
            attNumField8.setEditable(true);
        }
        else {
            attNumField8.setEditable(false);
        }
    }//GEN-LAST:event_attributeCombo8ActionPerformed

    private void attributeCombo9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attributeCombo9ActionPerformed
        if(attributeCombo9.getSelectedIndex() == 1) {
            attNumField9.setEditable(true);
        }
        else {
            attNumField9.setEditable(false);
        }
    }//GEN-LAST:event_attributeCombo9ActionPerformed

    private void attributeCombo5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attributeCombo5ActionPerformed
        if(attributeCombo5.getSelectedIndex() == 1) {
            attNumField5.setEditable(true);
        }
        else {
            attNumField5.setEditable(false);
        }
    }//GEN-LAST:event_attributeCombo5ActionPerformed

    private void attributeCombo4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attributeCombo4ActionPerformed
        if(attributeCombo4.getSelectedIndex() == 1) {
            attNumField4.setEditable(true);
        }
        else {
            attNumField4.setEditable(false);
        }
    }//GEN-LAST:event_attributeCombo4ActionPerformed

    private void attributeCombo6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attributeCombo6ActionPerformed
        if(attributeCombo6.getSelectedIndex() == 1) {
            attNumField6.setEditable(true);
        }
        else {
            attNumField6.setEditable(false);
        }
    }//GEN-LAST:event_attributeCombo6ActionPerformed

    private void attributeCombo11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attributeCombo11ActionPerformed
        if(attributeCombo11.getSelectedIndex() == 1) {
            attNumField11.setEditable(true);
        }
        else {
            attNumField11.setEditable(false);
        }
    }//GEN-LAST:event_attributeCombo11ActionPerformed

    private void attributeCombo12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attributeCombo12ActionPerformed
        if(attributeCombo12.getSelectedIndex() == 1) {
            attNumField12.setEditable(true);
        }
        else {
            attNumField12.setEditable(false);
        }
    }//GEN-LAST:event_attributeCombo12ActionPerformed

    private void attributeCombo10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attributeCombo10ActionPerformed
        if(attributeCombo10.getSelectedIndex() == 1) {
            attNumField10.setEditable(true);
        }
        else {
            attNumField10.setEditable(false);
        }
    }//GEN-LAST:event_attributeCombo10ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    //TODO: finish building SQL query
        String RHS;//= "Select * from graffiti where ";
        String LHS;
        RHS  = "";
        LHS  = "";
        //the following ungodly mess is brought to you by: Netbeans
        //and it's inability to allow me to package GUI elements as an array.
        String numberArray[] = new String[12];
        try {

            if(!attNumField1.getText().equals("")){
                //System.out.println("1: " + attNumField1.getText());
                numberArray[0] = Integer.toString(Integer.parseInt(attNumField1.getText()));
            }
            if(!attNumField2.getText().equals("")){
                //System.out.println("2: " + attNumField1.getText());
                numberArray[1] = Integer.toString(Integer.parseInt(attNumField2.getText()));
            }
            if(!attNumField3.getText().equals("")){
                //System.out.println("3: " + attNumField1.getText());
                numberArray[2] = Integer.toString(Integer.parseInt(attNumField3.getText()));
            }
            if(!attNumField4.getText().equals("")){
                //System.out.println("4: " + attNumField1.getText());
                numberArray[3] = Integer.toString(Integer.parseInt(attNumField4.getText()));
            }
            if(!attNumField5.getText().equals("")){
                //System.out.println("5: " + attNumField1.getText());
                numberArray[4] = Integer.toString(Integer.parseInt(attNumField5.getText()));
            }
            if(!attNumField6.getText().equals("")){
                //System.out.println("6: " + attNumField1.getText());
                numberArray[5] = Integer.toString(Integer.parseInt(attNumField6.getText()));
            }
            if(!attNumField7.getText().equals("")){
                //System.out.println("7: " + attNumField1.getText());
                numberArray[6] = Integer.toString(Integer.parseInt(attNumField7.getText()));
            }
            if(!attNumField8.getText().equals("")){
                //System.out.println("8: " + attNumField1.getText());
                numberArray[7] = Integer.toString(Integer.parseInt(attNumField8.getText()));
            }
            if(!attNumField9.getText().equals("")){
                //System.out.println("9: " + attNumField1.getText());
                numberArray[8] = Integer.toString(Integer.parseInt(attNumField9.getText()));
            }
            if(!attNumField10.getText().equals("")){
                //System.out.println("10: " + attNumField1.getText());
                numberArray[9] = Integer.toString(Integer.parseInt(attNumField10.getText()));
            }
            if(!attNumField11.getText().equals("")){
                //System.out.println("11: " + attNumField1.getText());
                numberArray[10] = Integer.toString(Integer.parseInt(attNumField11.getText()));
            }
            if(!attNumField12.getText().equals("")){
                //System.out.println("12: " + attNumField1.getText());
                numberArray[11] = Integer.toString(Integer.parseInt(attNumField12.getText()));

            }

        //end of ungodly mess~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

            for(int i = 0; i < 6; i++) {
                if(!boxModel[i].getSelectedItem().toString().equals("Select Attribute")) {
                    if(boxModel[i].getSelectedItem().toString().equals("Number")) {
                        LHS = LHS.concat(numberArray[i] + " ");
                        //System.out.println("LHS: " + LHS + Integer.toString(i));
                    }
                    else {
                        LHS = LHS.concat(boxModel[i].getSelectedItem().toString() + " ");
                        //System.out.println("LHS: " + LHS + Integer.toString(i));
                    }
                    if(i != 5 && !boxModel[i+1].getSelectedItem().toString().equals("Select Attribute")){
                        LHS = LHS.concat(opModel[i].getSelectedItem().toString() + " ");
                        //System.out.println(Integer.toString(i) + " - in");
                    }
                }
                else {
                    if(i != 5 && !boxModel[i+1].getSelectedItem().toString().equals("Select Attribute")
                            && !LHS.equals("")) {
                        LHS = LHS.concat(opModel[i].getSelectedItem().toString() + " ");
                        //System.out.println(Integer.toString(i) + " - out");
                    }
                }
            }

            for(int i = 6; i < 12; i++) {
                if(!boxModel[i].getSelectedItem().toString().equals("Select Attribute")) {
                    if(boxModel[i].getSelectedItem().toString().equals("Number")) {
                        RHS = RHS.concat(numberArray[i] + " ");
                        //System.out.println("RHS: " + RHS + Integer.toString(i));
                    }
                    else {
                        RHS = RHS.concat(boxModel[i].getSelectedItem().toString() + " ");
                        //System.out.println("RHS: " + RHS + Integer.toString(i));
                    }
                    if(i != 11 && !boxModel[i+1].getSelectedItem().toString().equals("Select Attribute")){
                        RHS = RHS.concat(opModel[i].getSelectedItem().toString() + " ");
                        //System.out.println(Integer.toString(i) + " - in");
                    }
                }
                else {
                    if(i != 11 && !boxModel[i+1].getSelectedItem().toString().equals("Select Attribute")
                            && !RHS.equals("")){
                        RHS = RHS.concat(opModel[i].getSelectedItem().toString() + " ");
                        //System.out.println(Integer.toString(i)+ " - out");
                    }
                }
            }
            System.out.println(LHS + " " + middleBox.getSelectedItem().toString() + " " + RHS);
        }
        catch(NumberFormatException nfe) {
            System.err.println("non-Integer entered in Integer field");
            nfe.printStackTrace();
        }
        //System.out.println(LHS + RHS);
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        final String[] arg = args;
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new G6ViewPlus(arg).setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox OpCombo1;
    private javax.swing.JComboBox OpCombo10;
    private javax.swing.JComboBox OpCombo11;
    private javax.swing.JComboBox OpCombo12;
    private javax.swing.JComboBox OpCombo2;
    private javax.swing.JComboBox OpCombo3;
    private javax.swing.JComboBox OpCombo4;
    private javax.swing.JComboBox OpCombo5;
    private javax.swing.JComboBox OpCombo6;
    private javax.swing.JComboBox OpCombo7;
    private javax.swing.JComboBox OpCombo8;
    private javax.swing.JComboBox OpCombo9;
    private javax.swing.JButton addInvariantButton;
    private javax.swing.JTextField attNumField1;
    private javax.swing.JTextField attNumField10;
    private javax.swing.JTextField attNumField11;
    private javax.swing.JTextField attNumField12;
    private javax.swing.JTextField attNumField2;
    private javax.swing.JTextField attNumField3;
    private javax.swing.JTextField attNumField4;
    private javax.swing.JTextField attNumField5;
    private javax.swing.JTextField attNumField6;
    private javax.swing.JTextField attNumField7;
    private javax.swing.JTextField attNumField8;
    private javax.swing.JTextField attNumField9;
    private javax.swing.JComboBox attributeCombo1;
    private javax.swing.JComboBox attributeCombo10;
    private javax.swing.JComboBox attributeCombo11;
    private javax.swing.JComboBox attributeCombo12;
    private javax.swing.JComboBox attributeCombo2;
    private javax.swing.JComboBox attributeCombo3;
    private javax.swing.JComboBox attributeCombo4;
    private javax.swing.JComboBox attributeCombo5;
    private javax.swing.JComboBox attributeCombo6;
    private javax.swing.JComboBox attributeCombo7;
    private javax.swing.JComboBox attributeCombo8;
    private javax.swing.JComboBox attributeCombo9;
    private javax.swing.JPanel attributePanel;
    private javax.swing.JTextField categoryNumberTextField;
    private javax.swing.JToggleButton deleteEdgeButton;
    private javax.swing.JToggleButton deleteVertexButton;
    private javax.swing.JComboBox edgeComboBox;
    private javax.swing.JList edgeList;
    private javax.swing.JScrollPane edgeListScrollPane;
    private javax.swing.JPanel edgeMenuPanel;
    private javax.swing.JSlider edgeSizeSlider;
    private javax.swing.JTextField endColumnTextField;
    private javax.swing.JPanel filePanel;
    private javax.swing.JButton graphGoButton;
    private javax.swing.JTextField graphNameTextField;
    private javax.swing.JTextField graphNumberTextField;
    private javax.swing.JToggleButton highlightingButton;
    private javax.swing.JToggleButton insertButton;
    private javax.swing.JList invariantList;
    private javax.swing.JScrollPane invariantListScrollPane;
    private javax.swing.JTextField invariantNumberTextField;
    private javax.swing.JTable invariantTable;
    public javax.swing.JScrollPane invariantTableScrollPane;
    private javax.swing.JTree invariantTree;
    private javax.swing.JScrollPane invariantTreeScrollPane;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton8;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JFrame jFrame1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JButton last10Button;
    private javax.swing.JPanel mainGraphPanel;
    private javax.swing.JComboBox middleBox;
    private javax.swing.JButton next10Button;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JButton refreshInvariantsButton;
    private javax.swing.JButton resetListButton;
    private javax.swing.JButton saveGraphButton;
    private javax.swing.JButton setEdgeButton;
    private javax.swing.JButton setVertexButton;
    private javax.swing.JTextField startColumnTextField;
    private javax.swing.JPanel tab1Panel;
    private javax.swing.JPanel tab2Panel;
    private javax.swing.JPanel tab3Panel;
    private javax.swing.JPanel tab5Panel;
    private javax.swing.JComboBox vertexComboBox;
    private javax.swing.JPanel vertexMenuPanel;
    private javax.swing.JSlider vertexSizeSlider;
    public javax.swing.JButton writeDriverButton;
    // End of variables declaration//GEN-END:variables
}
