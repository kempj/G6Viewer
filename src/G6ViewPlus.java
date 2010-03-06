
/*
 *  G6ViewPlus.java
 *
 *  Created on Jun 26, 2009, 4:17:10 PM
 *  By:  Hooman Hematti
 *      Justin D'souze
 *      Jeremy Kemp
 *  For Dr. Ermalinda Delavina
 * Delavinae@uhd.edu
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
import javax.swing.table.TableModel;


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
    int invCount = 0;
    int colCount = 0;
    int rowCount = 0;
    Object[][] data = {{"You","Shouldn't"},{"See","this"}};//object where invariant data is stored
   
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    private DefaultListModel graphListModel = new DefaultListModel();
    public String[] inputInv;

    //The program should behave properly if one of these elements is removed, or another added.
    private String[] graphTypeTitle = {"Original Graph","Complement of Graph",
                          "2nd Power of Graph","2-Core of Graph"};
    //invCat ==> Invariant Category
    private String[] invCatTitle = {"Basic Invariants","Degree Invariants",
                       "Distance Invariants","Vertex Subsets",
                       "Invariants on Edges","Subgraph Invariants",
                       "Properties"};
    public Vector[][] arrayG = new Vector[graphTypeTitle.length][invCatTitle.length];


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

        if(startIndex > endIndex || startIndex < 0)
        {//TODO: need to check for out of bounds error.
            endIndex = 10;
            startIndex = 0;
        }

        if(endIndex > colCount){
            endIndex = colCount - 1;
            startIndex = colCount - 11;
        }
        dataSmall = new Object[rowCount][endIndex-startIndex + 1];
        columnNames = new String[endIndex-startIndex + 1];
        columnNames[0] = " Graph Names ";

        int counter = 1;
        for(int i = startIndex ; i < endIndex ; i++)
        {
            columnNames[counter] = "";
            for(int j = 3;j < inputInv[i].split(" ").length;j++){
                columnNames[counter] = columnNames[counter] + inputInv[i].split(" ")[j];
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

    public void readInMCM()
    {
        FileInputStream fstream;
        DataInputStream in;
        BufferedReader br;
        String[] graphDataLine;

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
    {//places the string from the list into a 2D array of vectors.
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
        for(int i = 0; i < graphTypeTitle.length;i++)
        {
            for(int j = 0; j < invCatTitle.length; j++)
                arrayG[i][j] = new Vector();
        }
    }

    public void writeDriver()
    {
        //FileInputStream fstream;
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
                vb = jPanel1.getMousePosition();
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
                    temp.SetColor((String) jComboBox2.getSelectedItem());
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
            temp.SetColor((String) jComboBox1.getSelectedItem());
            vertices.add(temp);
            temp = new VisualVertex();
        }

    }

    public void DrawFromG6(String g6, JPanel p) {
        int size = GetGraphSize(g6);
        matrix = new int[size][size];
        GetMatrix(size, g6, matrix);
        RefreshVertexListForPanel(jPanel1);
        GetEdgeList(size, matrix);
        UpdateEdgeListDisplay();
        DrawGraph(jPanel1.getGraphics());
    }

    public void DrawGraph(Graphics g) {
        jPanel1.removeAll();
        jPanel1.paintAll(g);
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
        jList1.setModel(model);
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
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(colorset));
        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(colorset)); //reinitialize combobox
        jComboBox1.setEditable(false); //uneditable
        jComboBox2.setEditable(false); //uneditable

        //clean jTextField1
        jTextField1.setText("");

        //clean jList1
        jList1.setModel(new javax.swing.DefaultListModel());

        if (args.length != 0) {
            jTextField1.setText(args[0]);
            DrawFromG6(args[0], jPanel1);
        }

    }

    public G6ViewPlus() {
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
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(colorset));
        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(colorset)); //reinitialize combobox
        jComboBox1.setEditable(false); //uneditable
        jComboBox2.setEditable(false); //uneditable

        //clean jTextField1
        jTextField1.setText("");

        //clean jList1
        jList1.setModel(new javax.swing.DefaultListModel());


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
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jPanel1 = new javax.swing.JPanel();
        jToggleButton1 = new javax.swing.JToggleButton();
        jPanel2 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jToggleButton2 = new javax.swing.JToggleButton();
        jComboBox1 = new javax.swing.JComboBox();
        jSlider1 = new javax.swing.JSlider();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jButton3 = new javax.swing.JButton();
        jToggleButton3 = new javax.swing.JToggleButton();
        jComboBox2 = new javax.swing.JComboBox();
        jSlider2 = new javax.swing.JSlider();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jToggleButton4 = new javax.swing.JToggleButton();
        jButton1 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jButton8 = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        readInInv();
        jTree1 = new javax.swing.JTree();
        jButton5 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jButton6 = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList(graphListModel);
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jTextField6 = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        jButton7 = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jButton14 = new javax.swing.JButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jLabel11 = new javax.swing.JLabel();
        jButton13 = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jButton12 = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();

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

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList1.setFocusCycleRoot(true);
        jList1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jList1MouseClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jList1MouseReleased(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel1MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jPanel1MouseEntered(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jPanel1MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jPanel1MouseReleased(evt);
            }
        });
        jPanel1.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                jPanel1ComponentResized(evt);
            }
        });
        jPanel1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                jPanel1MouseDragged(evt);
            }
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jPanel1MouseMoved(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 517, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 565, Short.MAX_VALUE)
        );

        jToggleButton1.setText("Insert");
        jToggleButton1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jToggleButton1StateChanged(evt);
            }
        });
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Vertex"));

        jButton2.setText("Set All");
        jButton2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jToggleButton2.setText("Delete Mode");
        jToggleButton2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jToggleButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton2ActionPerformed(evt);
            }
        });

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jSlider1.setMajorTickSpacing(10);
        jSlider1.setMaximum(60);
        jSlider1.setMinimum(10);
        jSlider1.setMinorTickSpacing(5);
        jSlider1.setPaintLabels(true);
        jSlider1.setPaintTicks(true);
        jSlider1.setSnapToTicks(true);
        jSlider1.setValue(10);
        jSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider1StateChanged(evt);
            }
        });

        jLabel1.setText("Size");

        jLabel2.setText("Color");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jComboBox1, 0, 109, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSlider1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
                    .addComponent(jToggleButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addGap(5, 5, 5)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jButton4.setText("Save");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Edge"));

        jButton3.setText("Set All");
        jButton3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jToggleButton3.setText("Delete Mode");
        jToggleButton3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jToggleButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton3ActionPerformed(evt);
            }
        });

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox2ActionPerformed(evt);
            }
        });

        jSlider2.setMajorTickSpacing(10);
        jSlider2.setMaximum(35);
        jSlider2.setMinimum(5);
        jSlider2.setMinorTickSpacing(5);
        jSlider2.setPaintLabels(true);
        jSlider2.setPaintTicks(true);
        jSlider2.setSnapToTicks(true);
        jSlider2.setValue(5);
        jSlider2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider2StateChanged(evt);
            }
        });

        jLabel3.setText("Color");

        jLabel4.setText("Size");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jComboBox2, 0, 109, Short.MAX_VALUE)
                    .addComponent(jToggleButton3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSlider2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSlider2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jToggleButton4.setText("Highlighting");
        jToggleButton4.setToolTipText("When highligting is activated, the edit functionalities (if in Edit mode), will be disabled.");
        jToggleButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton4ActionPerformed(evt);
            }
        });

        jButton1.setText("Go");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTextField1.setText("jTextField1");
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jButton8.setText("Reset");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jTextField1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 613, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToggleButton4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jToggleButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );

        jPanel4Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButton1, jButton4, jButton8, jToggleButton1});

        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jToggleButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jToggleButton1)
                            .addComponent(jButton4)))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton8)
                        .addComponent(jButton1)))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("G6 Viewer", jPanel4);

        initializeVectorArray();
        rootNode = new DefaultMutableTreeNode("All Invariants");
        int k = 0;
        int counter = 0;
        String displayName;
        /*String[] tempGraphType = {"Original Graph","Complement of Graph",
            "2nd Power of Graph","2-Core of Graph"};
        //invCat ==> Invariant Category
        String[] tempInvCat = {"Basic Invariants","Degree Invariants",
            "Distance Invariants","Vertex Subsets",
            "Invariants on Edges","Subgraph Invariants",
            "Properties"};*/

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
        jTree1.setModel(treeModel);
        jTree1.setEditable(true);
        jTree1.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTree1ValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jTree1);

        //ActionListener done;
        jButton5.setText("Write");
        //jButton5.setEnabled(false);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton9.setText("Reset");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jLabel6.setText("Manual Input:");

        jLabel7.setText("Graph");

        jLabel8.setText("Category");

        jLabel9.setText("Invariant");

        jButton6.setText("Add");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        /*
        jList2.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { ""};//Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        */
        jScrollPane5.setViewportView(jList2);

        jTextField6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField6ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 402, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addGap(4, 4, 4)
                                .addComponent(jLabel7)
                                .addGap(2, 2, 2)
                                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(32, 32, 32))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                                .addContainerGap()))
                        .addGroup(jPanel7Layout.createSequentialGroup()
                            .addComponent(jButton6)
                            .addContainerGap()))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addContainerGap())))
        );

        jPanel7Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jTextField4, jTextField5, jTextField6});

        jPanel7Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel7, jLabel8, jLabel9});

        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 404, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel6)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7)
                            .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9)
                            .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(11, 11, 11)
                        .addComponent(jButton6))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 539, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton5)
                .addGap(14, 14, 14))
        );

        /*done = new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                jButton5.setEnabled(false);
                jButton6.setEnabled(true);
                selection = false;

                try
                {
                    FileOutputStream fos = new FileOutputStream("write.txt");
                    DataOutputStream dos = new DataOutputStream(fos);
                    int y=0;
                    while(select_array[y] != null)
                    {
                        if(number_array[y] == 0)
                        dos.writeChars("000");
                        else if(number_array[y] > 0 && number_array[y]<10)
                        {
                            dos.writeChars("00");
                            dos.writeInt(number_array[y]);
                        }
                        else if(number_array[y] > 9 && number_array[y]<99)
                        {
                            dos.writeChar('0');
                            dos.writeInt(number_array[y]);
                        }
                        dos.writeChar(' ');
                        dos.writeChars(select_array[y]);
                        dos.writeChar('\n');
                        y++;
                    }
                    dos.close();
                }
                catch (IOException b){}
                return;
            }
        };

        jButton5.addActionListener(done);*/

        jTabbedPane1.addTab("Select Invariants", jPanel7);

        jButton7.setText("Refresh");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jScrollPane3.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane3.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane3.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                jScrollPane3MouseWheelMoved(evt);
            }
        });

        //readInData();
        readInMCM();
        showSubsetData(0,10);
        jTable1.setModel(tModel1);
        jScrollPane3.setViewportView(jTable1);

        jTextField2.setText("0");
        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });

        jTextField3.setText("10");
        jTextField3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField3ActionPerformed(evt);
            }
        });

        jLabel5.setText(" -");

        jButton10.setText("Next 10");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        jButton11.setText("Previous 10");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 764, Short.MAX_VALUE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(32, 32, 32)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jButton11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 592, Short.MAX_VALUE)
                        .addComponent(jButton10)))
                .addContainerGap())
        );

        jPanel5Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButton10, jButton11});

        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 521, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton11)
                    .addComponent(jButton10))
                .addContainerGap(22, Short.MAX_VALUE))
        );

        /*refresh = new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                String[] new_columnname = { "Graphs", select_array[0],
                    select_array[1],
                    select_array[2],
                    select_array[3]
                };
                Object[][] new_data = {
                    {null, null, null, null, null, null},
                    {null, null, null, null, null, null},
                    {null, null, null, null, null, null},
                    {null, null, null, null, null, null},
                    {null, null, null, null, null, null}
                };*/
                /*return;
            }
        };*/

        jTabbedPane1.addTab("Inspect Invariants", jPanel5);
        jPanel5.setVisible(true);

        jLabel12.setText("Execution and Output");

        jButton14.setText("Run");

        jCheckBox1.setText("Write to file");

        jCheckBox2.setText("View output in Tab 3");

        jCheckBox3.setText("Append to Existing data");

        jLabel11.setText("Choose Driver File");

        jButton13.setText("Find File");

        jLabel10.setText("Select Graph File");

        jButton12.setText("Find File");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jCheckBox3)
                        .addComponent(jCheckBox2)
                        .addComponent(jCheckBox1))
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel6Layout.createSequentialGroup()
                            .addComponent(jLabel10)
                            .addGap(18, 18, 18)
                            .addComponent(jButton12))
                        .addGroup(jPanel6Layout.createSequentialGroup()
                            .addComponent(jLabel11)
                            .addGap(18, 18, 18)
                            .addComponent(jButton13))
                        .addGroup(jPanel6Layout.createSequentialGroup()
                            .addComponent(jLabel12)
                            .addGap(18, 18, 18)
                            .addComponent(jButton14))))
                .addContainerGap(557, Short.MAX_VALUE))
        );

        jPanel6Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel10, jLabel11, jLabel12});

        jPanel6Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButton12, jButton13, jButton14});

        jPanel6Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jCheckBox1, jCheckBox2, jCheckBox3});

        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addGap(64, 64, 64)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jButton12))
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jButton13))
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(jButton14))
                .addGap(18, 18, 18)
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox3)
                .addGap(345, 345, 345))
        );

        jTabbedPane1.addTab("Run Program", jPanel6);

        jMenu1.setText("File");

        jMenuItem1.setText("Launch Organizer");
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Tools");

        jMenuItem2.setText("Pendants");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem2);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 792, Short.MAX_VALUE)
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
        DrawGraph(jPanel1.getGraphics());
    }//GEN-LAST:event_formWindowActivated

    private void formComponentMoved(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentMoved
        DrawGraph(jPanel1.getGraphics());
    }//GEN-LAST:event_formComponentMoved

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        String[] args = {"", ""};
        Tools.Pendants.main(args);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // Old stuff
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        /*NODE: Removing other modes may inconvinience user*/
        //        jToggleButton1.setSelected(false); //edit mode set to off
        //        jToggleButton2.setSelected(false); //vertex delete mode set to off
        //        jToggleButton3.setSelected(false); //edge delete mode set to off
        //        jToggleButton4.setSelected(false); //edit highlight mode set to off
        String g6 = jTextField1.getText();
        if (!g6.isEmpty()) {
            DrawFromG6(g6, jPanel1);
        }
}//GEN-LAST:event_jButton1ActionPerformed

    private void jToggleButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton4ActionPerformed
        if (jToggleButton4.isSelected()) {
            jToggleButton1.setSelected(false);
            jToggleButton2.setSelected(false);
            jToggleButton3.setSelected(false);
        }
}//GEN-LAST:event_jToggleButton4ActionPerformed

    private void jSlider2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider2StateChanged
        edge_width = jSlider2.getValue();
        VisualEdge temp;
        for (int i = 0; i < edges.size(); i++) {
            temp = (VisualEdge) edges.get(i);
            temp.SetWidth(jSlider2.getValue());
        }
        DrawGraph(jPanel1.getGraphics());
}//GEN-LAST:event_jSlider2StateChanged

    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed
        if (jToggleButton4.isSelected() && !jList1.isSelectionEmpty()) {
            VisualEdge e;
            int[] selected = jList1.getSelectedIndices();
            for (int i = 0; i < selected.length; i++) {
                e = (VisualEdge) edges.get(selected[i]);
                e.SetColor((String) jComboBox2.getSelectedItem());
            }
            DrawGraph(jPanel1.getGraphics());
        }
}//GEN-LAST:event_jComboBox2ActionPerformed

    private void jToggleButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton3ActionPerformed
        if (jToggleButton2.isSelected()) {
            jToggleButton1.setSelected(false); //turn off edit mode
            jToggleButton4.setSelected(false); //turn off highlight mode
        }
}//GEN-LAST:event_jToggleButton3ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        VisualEdge e;

        for (int i = 0; i < edges.size(); i++) {
            e = (VisualEdge) edges.get(i);
            e.SetColor((String) jComboBox2.getSelectedItem());
        }

        DrawGraph(jPanel1.getGraphics());
}//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed


        String filename = JOptionPane.showInputDialog("Please enter a name for your graph");
        File saveFile = new File(filename + ".jpg");
        //Image drawing = createImage(jPanel1.getSize().width,jPanel1.getSize().height);
        BufferedImage bi = new BufferedImage(jPanel1.getSize().width, jPanel1.getSize().height, BufferedImage.TYPE_INT_RGB);
        Graphics g = bi.getGraphics();
        DrawGraph(g);
        try {
            ImageIO.write(bi, "jpg", saveFile);
        } catch (IOException ex) {
            Logger.getLogger(G6ViewPlus.class.getName()).log(Level.SEVERE, null, ex);
        }
        g.dispose();



        String g6Text = jTextField1.getText();
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
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jSlider1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider1StateChanged
        vertex_diam = jSlider1.getValue();
        VisualVertex temp;
        for (int i = 0; i < vertices.size(); i++) {
            temp = (VisualVertex) vertices.get(i);
            temp.SetDiam(jSlider1.getValue());
        }
        DrawGraph(jPanel1.getGraphics());
}//GEN-LAST:event_jSlider1StateChanged

    private void jToggleButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton2ActionPerformed
        if (jToggleButton2.isSelected()) {
            jToggleButton1.setSelected(false); //turn off edit mode
            jToggleButton4.setSelected(false); //turn off highlight mode
        }
}//GEN-LAST:event_jToggleButton2ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        VisualVertex v;

        for (int i = 0; i < vertices.size(); i++) {
            v = (VisualVertex) vertices.get(i);
            v.SetColor((String) jComboBox1.getSelectedItem());
        }

        DrawGraph(jPanel1.getGraphics());
}//GEN-LAST:event_jButton2ActionPerformed

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        if (jToggleButton1.isSelected()) {
            jToggleButton4.setSelected(false);
            jToggleButton3.setSelected(false);
            jToggleButton2.setSelected(false);

        }
}//GEN-LAST:event_jToggleButton1ActionPerformed

    private void jToggleButton1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jToggleButton1StateChanged
        //if(jToggleButton1.isSelected())
        //{
        UpdateMatrix();
        jTextField1.setText("");
        jTextField1.setText(GetG6(matrix));
        //}
}//GEN-LAST:event_jToggleButton1StateChanged

    private void jPanel1MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MouseMoved
        int old = selected_index;
        selected_index = VertexInLocation(evt.getPoint());
        if (old != selected_index) {
            DrawGraph(jPanel1.getGraphics());
        }
}//GEN-LAST:event_jPanel1MouseMoved

    private void jPanel1MouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MouseDragged
        if (jToggleButton1.isSelected() && !evt.isShiftDown()) {
            selected_index = VertexInLocation(evt.getPoint());
            DrawGraph(jPanel1.getGraphics());
        } else if (selected_index != -1) {
            VisualVertex temp;
            temp = (VisualVertex) vertices.get(selected_index);
            temp.SetCenter(jPanel1.getMousePosition());
            DrawGraph(jPanel1.getGraphics());
        }
}//GEN-LAST:event_jPanel1MouseDragged

    private void jPanel1ComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPanel1ComponentResized
        DrawGraph(jPanel1.getGraphics());
}//GEN-LAST:event_jPanel1ComponentResized

    private void jPanel1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MouseReleased
        if (jToggleButton1.isSelected() && //is in edit mode
                !evt.isShiftDown() && //shift is not held
                evt.getButton() == MouseEvent.BUTTON1) //the left mouse button is released
        {
            VisualEdge e;
            e = (VisualEdge) edges.get(edges.size() - 1);

            if (e.GetB() == -2) {
                if (selected_index == -1) {
                    VisualVertex n = new VisualVertex();
                    n.SetColor((String) jComboBox1.getSelectedItem());
                    n.SetCenter(evt.getPoint());
                    n.SetDiam(jSlider1.getValue());
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
            jTextField1.setText("");
            jTextField1.setText(GetG6(matrix));
            DrawGraph(jPanel1.getGraphics());
            UpdateEdgeListDisplay();
        }
}//GEN-LAST:event_jPanel1MouseReleased

    private void jPanel1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MousePressed
        //Click and hold: Always for moving nodes

        //this bit is redundant sincehovering selects vertex
        /*     if(selected_index == -1)
        selected_index = VertexInLocation(evt.getPoint());*/

        //edit mode is active&& shift isn't pressed, create edge
        if (jToggleButton1.isSelected() && !evt.isShiftDown() && selected_index != -1) {
            if (evt.getButton() == MouseEvent.BUTTON1) //left?
            {
                VisualEdge n = new VisualEdge();
                n.SetA(selected_index);
                n.SetB(-2); //code for mouse location
                n.SetColor((String) jComboBox2.getSelectedItem());
                n.SetWidth(jSlider2.getValue());
                edges.add(n);
            } else//other button
            {
            }
        }
    }//GEN-LAST:event_jPanel1MousePressed

    private void jPanel1MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MouseEntered
        DrawGraph(jPanel1.getGraphics());
}//GEN-LAST:event_jPanel1MouseEntered

    private void jPanel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MouseClicked
        if (jToggleButton2.isSelected()) {//vertex delete mode selected
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
                jTextField1.setText("");
                jTextField1.setText(GetG6(matrix));
                UpdateEdgeListDisplay();
                DrawGraph(jPanel1.getGraphics());
            }

        } else if (jToggleButton3.isSelected()) //delete edge mode is uniquely selected
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
                jList1.setSelectedIndices(index_array);
                // DrawGraph(jPanel1.getGraphics()); //Note rendered highlighting of edges yet
            }

        }

        if (jToggleButton4.isSelected()) {
            if (selected_index != -1) {
                VisualVertex v;
                v = (VisualVertex) vertices.get(selected_index);
                v.SetColor((String) jComboBox1.getSelectedItem());
                DrawGraph(jPanel1.getGraphics());
            }
        }
        if (jToggleButton1.isSelected())//edit mode is active
        {
            if (evt.getButton() == MouseEvent.BUTTON1 && selected_index == -1) //left?
            {
                VisualVertex n = new VisualVertex();
                n.SetColor((String) jComboBox1.getSelectedItem());
                n.SetCenter(evt.getPoint());
                n.SetDiam(jSlider1.getValue());
                vertices.add(n);

                selected_index = vertices.size() - 1;
                UpdateMatrix();
                jTextField1.setText("");
                jTextField1.setText(GetG6(matrix));
                DrawGraph(jPanel1.getGraphics());
            } else//other button
            {
            }
        }
}//GEN-LAST:event_jPanel1MouseClicked

    private void jList1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MouseReleased
        if (!evt.isShiftDown() && !evt.isControlDown()) {
            jList1.clearSelection();
        }
}//GEN-LAST:event_jList1MouseReleased

    private void jList1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MouseClicked
        //BUG: id clicked on empty space below last edge
        //last edge is highlighted
        if (jToggleButton4.isSelected() && !evt.isControlDown() && !evt.isShiftDown()) {
            VisualEdge e;
            int index = jList1.locationToIndex(jList1.getMousePosition());
            if (index != -1) {
                e = (VisualEdge) edges.get(index);
                e.SetColor((String) jComboBox2.getSelectedItem());
                DrawGraph(jPanel1.getGraphics());
            }
        }
        if (jToggleButton3.isSelected())//in delete edge mode
        {
            int index = jList1.locationToIndex(jList1.getMousePosition());
            if (index != -1) {
                edges.remove(index);
            }

            UpdateMatrix();
            jTextField1.setText("");
            jTextField1.setText(GetG6(matrix));
            UpdateEdgeListDisplay();
            DrawGraph(jPanel1.getGraphics());

        }
}//GEN-LAST:event_jList1MouseClicked

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        //Updating data for tab 3 to desired range
        showSubsetData(Integer.parseInt(jTextField2.getText()), Integer.parseInt(jTextField3.getText()));
}//GEN-LAST:event_jButton7ActionPerformed

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
            jTextField1.setText("");
            jTextField1.setText(GetG6(matrix));
            UpdateEdgeListDisplay();
            DrawGraph(jPanel1.getGraphics());
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        //convert strings to appropriate numbers
        for(int i=0; i < graphListModel.size();i++)
        {
            convertToArray(graphListModel.get(i).toString());
        }
        writeDriver();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // Not going to do anything, textfield just for entering data
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed
        // Not going to do anything, textfield just for entering data
    }//GEN-LAST:event_jTextField3ActionPerformed

    private void jTextField6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField6ActionPerformed
        // Not going to do anything, textfield just for entering data
    }//GEN-LAST:event_jTextField6ActionPerformed

    private void jTree1ValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jTree1ValueChanged
                DefaultMutableTreeNode readNode = (DefaultMutableTreeNode)
                           jTree1.getLastSelectedPathComponent();
        if (readNode == null)
            return;

        Object nodeInfo = readNode.getUserObject();
        if (readNode.isLeaf() && !graphListModel.contains(nodeInfo.toString() + "::" + readNode.getParent().getParent().toString()))
        {
            graphListModel.addElement(nodeInfo.toString() + "::" + readNode.getParent().getParent().toString());

        }
    }//GEN-LAST:event_jTree1ValueChanged

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        //TODO:input checking
        if (!graphListModel.contains("Custom Invariant: " + jTextField4.getText() + " " + jTextField5.getText() + " " + jTextField6.getText()))
            graphListModel.addElement("Custom Invariant: " + jTextField4.getText() + " " + jTextField5.getText() + " " + jTextField6.getText());
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        jTextField2.setText(Integer.toString(Integer.parseInt(jTextField2.getText()) + 10));
        jTextField3.setText(Integer.toString(Integer.parseInt(jTextField3.getText()) + 10));
        showSubsetData(Integer.parseInt(jTextField2.getText()), Integer.parseInt(jTextField3.getText()));

    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        graphListModel.removeAllElements();
        initializeVectorArray();
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jScrollPane3MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_jScrollPane3MouseWheelMoved
        // need to figure out how to get rid of this.
    }//GEN-LAST:event_jScrollPane3MouseWheelMoved

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        jTextField2.setText(Integer.toString(Integer.parseInt(jTextField2.getText()) - 10));
        jTextField3.setText(Integer.toString(Integer.parseInt(jTextField3.getText()) - 10));
        showSubsetData(Integer.parseInt(jTextField2.getText()), Integer.parseInt(jTextField3.getText()));
    }//GEN-LAST:event_jButton11ActionPerformed

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
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    public javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JFrame jFrame1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JList jList1;
    private javax.swing.JList jList2;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    public javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JSlider jSlider2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToggleButton jToggleButton2;
    private javax.swing.JToggleButton jToggleButton3;
    private javax.swing.JToggleButton jToggleButton4;
    private javax.swing.JTree jTree1;
    // End of variables declaration//GEN-END:variables
}
