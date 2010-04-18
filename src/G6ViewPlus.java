
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
import javax.swing.table.TableModel;
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
        jPanel1 = new javax.swing.JPanel();
        jTextField4 = new javax.swing.JTextField();
        jComboBox15 = new javax.swing.JComboBox();
        jTextField9 = new javax.swing.JTextField();
        jComboBox10 = new javax.swing.JComboBox();
        jTextField13 = new javax.swing.JTextField();
        jTextField6 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jComboBox8 = new javax.swing.JComboBox();
        jComboBox23 = new javax.swing.JComboBox();
        jTextField3 = new javax.swing.JTextField();
        jTextField1 = new javax.swing.JTextField();
        jTextField8 = new javax.swing.JTextField();
        jComboBox14 = new javax.swing.JComboBox();
        jComboBox5 = new javax.swing.JComboBox();
        jTextField2 = new javax.swing.JTextField();
        jComboBox17 = new javax.swing.JComboBox();
        jComboBox16 = new javax.swing.JComboBox();
        jComboBox13 = new javax.swing.JComboBox();
        jTextField5 = new javax.swing.JTextField();
        jComboBox18 = new javax.swing.JComboBox();
        jComboBox4 = new javax.swing.JComboBox();
        jComboBox22 = new javax.swing.JComboBox();
        jComboBox3 = new javax.swing.JComboBox();
        jComboBox7 = new javax.swing.JComboBox();
        jTextField12 = new javax.swing.JTextField();
        jComboBox25 = new javax.swing.JComboBox();
        jComboBox19 = new javax.swing.JComboBox();
        jTextField10 = new javax.swing.JTextField();
        jTextField11 = new javax.swing.JTextField();
        jComboBox20 = new javax.swing.JComboBox();
        jComboBox1 = new javax.swing.JComboBox();
        jComboBox6 = new javax.swing.JComboBox();
        jComboBox12 = new javax.swing.JComboBox();
        jComboBox9 = new javax.swing.JComboBox();
        jComboBox11 = new javax.swing.JComboBox();
        jComboBox21 = new javax.swing.JComboBox();
        jComboBox2 = new javax.swing.JComboBox();
        jComboBox24 = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();
        jButton14 = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jButton12 = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        jButton13 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
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
            .addGap(0, 565, Short.MAX_VALUE)
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
        invariantTree.setModel(treeModel);
        invariantTree.setEditable(true);
        invariantTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                invariantTreeValueChanged(evt);
            }
        });
        invariantTreeScrollPane.setViewportView(invariantTree);

        //ActionListener done;
        writeDriverButton.setText("Write");
        //jButton5.setEnabled(false);
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
                    .addComponent(invariantTreeScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 547, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(writeDriverButton)
                .addGap(14, 14, 14))
        );

        /*done = new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                writeDriverButton.setEnabled(false);
                addInvariantButton.setEnabled(true);
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

        writeDriverButton.addActionListener(done);*/

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
        readInMCM();
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

        tab3PanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {last10Button, next10Button});

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

        jTabbedPane1.addTab("Inspect Invariants", tab3Panel);
        //tab3Panel.setVisible(true);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jTextField4.setEditable(false);

        jComboBox15.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "+", "-", "*", "/" }));

        jTextField1.setEditable(false);

        jComboBox10.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Attribute 1", "Attribute 2", "Attribute 3", "Attribute 4" }));
        jComboBox10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox10ActionPerformed(evt);
            }
        });

        jTextField5.setEditable(false);

        jTextField6.setEditable(false);

        jButton1.setText("Search for Graph");

        jComboBox8.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "+", "-", "*", "/" }));

        jComboBox23.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "+", "-", "*", "/" }));

        jTextField3.setEditable(false);

        jTextField1.setEditable(false);

        jTextField2.setEditable(false);

        jComboBox14.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Attribute 1", "Attribute 2", "Attribute 3", "Attribute 4" }));
        jComboBox14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox14ActionPerformed(evt);
            }
        });

        jComboBox5.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Attribute 1", "Attribute 2", "Attribute 3", "Attribute 4" }));
        jComboBox5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox5ActionPerformed(evt);
            }
        });

        jTextField2.setEditable(false);

        jComboBox17.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Attribute 1", "Attribute 2", "Attribute 3", "Attribute 4" }));
        jComboBox17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox17ActionPerformed(evt);
            }
        });

        jComboBox16.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "+", "-", "*", "/" }));

        jComboBox13.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<", ">", "=", "<=", "=>", "!=" }));

        jTextField5.setEditable(false);

        jComboBox18.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "+", "-", "*", "/" }));

        jComboBox4.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Attribute 1", "Attribute 2", "Attribute 3", "Attribute 4" }));
        jComboBox4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox4ActionPerformed(evt);
            }
        });

        jComboBox22.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "+", "-", "*", "/" }));

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "+", "-", "*", "/" }));

        jComboBox7.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "+", "-", "*", "/" }));

        jTextField4.setEditable(false);

        jComboBox25.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Attribute 1", "Attribute 2", "Attribute 3", "Attribute 4" }));
        jComboBox25.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox25ActionPerformed(evt);
            }
        });

        jComboBox19.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Attribute 1", "Attribute 2", "Attribute 3", "Attribute 4" }));
        jComboBox19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox19ActionPerformed(evt);
            }
        });

        jTextField3.setEditable(false);

        jTextField6.setEditable(false);

        jComboBox20.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Attribute 1", "Attribute 2", "Attribute 3", "Attribute 4" }));
        jComboBox20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox20ActionPerformed(evt);
            }
        });

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Attribute 1", "Attribute 2", "Attribute 3", "Attribute 4" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jComboBox6.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "+", "-", "*", "/" }));

        jComboBox12.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "+", "-", "*", "/" }));

        jComboBox9.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Attribute 1", "Attribute 2", "Attribute 3", "Attribute 4" }));
        jComboBox9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox9ActionPerformed(evt);
            }
        });

        jComboBox11.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Attribute 1", "Attribute 2", "Attribute 3", "Attribute 4" }));
        jComboBox11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox11ActionPerformed(evt);
            }
        });

        jComboBox21.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Attribute 1", "Attribute 2", "Attribute 3", "Attribute 4" }));
        jComboBox21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox21ActionPerformed(evt);
            }
        });

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "+", "-", "*", "/" }));

        jComboBox24.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "+", "-", "*", "/" }));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(58, 58, 58)
                        .addComponent(jButton1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBox19, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox17, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox14, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox13, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField10, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBox18, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox16, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox15, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBox11, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox10, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox20, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox25, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox21, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox9, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField11, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField12, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField13, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBox12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jComboBox24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jComboBox22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jComboBox23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jComboBox8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jComboBox7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jComboBox12, jComboBox2, jComboBox3, jComboBox6, jComboBox7, jComboBox8});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addComponent(jComboBox16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jComboBox18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jComboBox15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jComboBox17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboBox14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboBox19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jComboBox13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboBox9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jComboBox25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboBox20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboBox21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jTextField13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addComponent(jComboBox23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jComboBox22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jComboBox24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jComboBox1, jComboBox10, jComboBox11, jComboBox12, jComboBox13, jComboBox2, jComboBox3, jComboBox4, jComboBox5, jComboBox6, jComboBox7, jComboBox8, jComboBox9, jTextField1, jTextField2, jTextField3, jTextField4, jTextField5, jTextField6});

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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addGap(18, 18, 18)
                                .addComponent(jButton13))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addGap(18, 18, 18)
                                .addComponent(jButton12))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(70, 70, 70)
                        .addComponent(jButton14)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel10, jLabel11});

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButton12, jButton13, jButton14});

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addComponent(jButton12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jButton13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 92, Short.MAX_VALUE)
                .addComponent(jButton14)
                .addContainerGap())
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jButton12, jButton13, jButton14});

        jPanel2Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jLabel10, jLabel11});

        jCheckBox2.setText("View output in Tab 3");

        jCheckBox1.setText("Write to file");

        jLabel13.setText("Choose Output File Name");

        jLabel12.setText("Options");

        jCheckBox3.setText("Append to Existing data");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox1)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel13)))
                    .addComponent(jLabel12)
                    .addComponent(jCheckBox2)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jCheckBox3)))
                .addContainerGap(55, Short.MAX_VALUE))
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jCheckBox1, jCheckBox2, jCheckBox3});

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
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

        jPanel3Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jCheckBox1, jCheckBox2});

        javax.swing.GroupLayout tab5PanelLayout = new javax.swing.GroupLayout(tab5Panel);
        tab5Panel.setLayout(tab5PanelLayout);
        tab5PanelLayout.setHorizontalGroup(
            tab5PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tab5PanelLayout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addGroup(tab5PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(tab5PanelLayout.createSequentialGroup()
                        .addGroup(tab5PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(41, 41, 41))
                    .addGroup(tab5PanelLayout.createSequentialGroup()
                        .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGap(28, 28, 28)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(92, 92, 92))
        );
        tab5PanelLayout.setVerticalGroup(
            tab5PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tab5PanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tab5PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 541, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, tab5PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, tab5PanelLayout.createSequentialGroup()
                            .addGap(27, 27, 27)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 56, Short.MAX_VALUE)
                            .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(37, 37, 37)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(57, Short.MAX_VALUE))
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
        //TODO:input checking
        if(Integer.parseInt(graphNumberTextField.getText()) < 4 && Integer.parseInt(categoryNumberTextField.getText()) < 7){
            if (!graphListModel.contains("Custom Invariant: " + graphNumberTextField.getText() + " " + categoryNumberTextField.getText() + " " + invariantNumberTextField.getText()))
                graphListModel.addElement("Custom Invariant: " + graphNumberTextField.getText() + " " + categoryNumberTextField.getText() + " " + invariantNumberTextField.getText());
        }
    }//GEN-LAST:event_addInvariantButtonActionPerformed

    private void next10ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_next10ButtonActionPerformed
        startColumnTextField.setText(Integer.toString(Integer.parseInt(startColumnTextField.getText()) + 10));
        endColumnTextField.setText(Integer.toString(Integer.parseInt(endColumnTextField.getText()) + 10));
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
        if(Integer.parseInt(startColumnTextField.getText()) < 10) {
            startColumnTextField.setText("0");
        }
        else
        {
        startColumnTextField.setText(Integer.toString(Integer.parseInt(startColumnTextField.getText()) - 10));
        }
        if(Integer.parseInt(startColumnTextField.getText()) < 10) {
            endColumnTextField.setText("10");
        }
        else {
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

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
        if(jComboBox1.getSelectedIndex() == 1) {
            jTextField1.setEditable(true);
        }
        else {
            jTextField1.setEditable(false);
        }
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jComboBox4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox4ActionPerformed
        if(jComboBox4.getSelectedIndex() == 1) {
            jTextField2.setEditable(true);
        }
        else {
            jTextField2.setEditable(false);
        }
    }//GEN-LAST:event_jComboBox4ActionPerformed

    private void jComboBox5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox5ActionPerformed
        if(jComboBox5.getSelectedIndex() == 1) {
            jTextField3.setEditable(true);
        }
        else {
            jTextField3.setEditable(false);
        }
    }//GEN-LAST:event_jComboBox5ActionPerformed

    private void jComboBox9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox9ActionPerformed
        if(jComboBox9.getSelectedIndex() == 1) {
            jTextField5.setEditable(true);
        }
        else {
            jTextField5.setEditable(false);
        }
    }//GEN-LAST:event_jComboBox9ActionPerformed

    private void jComboBox11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox11ActionPerformed
        if(jComboBox11.getSelectedIndex() == 1) {
            jTextField4.setEditable(true);
        }
        else {
            jTextField4.setEditable(false);
        }
    }//GEN-LAST:event_jComboBox11ActionPerformed

    private void jComboBox10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox10ActionPerformed
        if(jComboBox10.getSelectedIndex() == 1) {
            jTextField6.setEditable(true);
        }
        else {
            jTextField6.setEditable(false);
        }
    }//GEN-LAST:event_jComboBox10ActionPerformed

    private void jComboBox14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox14ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox14ActionPerformed

    private void jComboBox17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox17ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox17ActionPerformed

    private void jComboBox19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox19ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox19ActionPerformed

    private void jComboBox20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox20ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox20ActionPerformed

    private void jComboBox21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox21ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox21ActionPerformed

    private void jComboBox25ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox25ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox25ActionPerformed

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
    private javax.swing.JButton addInvariantButton;
    private javax.swing.JTextField categoryNumberTextField;
    private javax.swing.JToggleButton deleteEdgeButton;
    private javax.swing.JToggleButton deleteVertexButton;
    private javax.swing.JComboBox edgeComboBox;
    private javax.swing.JList edgeList;
    private javax.swing.JScrollPane edgeListScrollPane;
    private javax.swing.JPanel edgeMenuPanel;
    private javax.swing.JSlider edgeSizeSlider;
    private javax.swing.JTextField endColumnTextField;
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
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox10;
    private javax.swing.JComboBox jComboBox11;
    private javax.swing.JComboBox jComboBox12;
    private javax.swing.JComboBox jComboBox13;
    private javax.swing.JComboBox jComboBox14;
    private javax.swing.JComboBox jComboBox15;
    private javax.swing.JComboBox jComboBox16;
    private javax.swing.JComboBox jComboBox17;
    private javax.swing.JComboBox jComboBox18;
    private javax.swing.JComboBox jComboBox19;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JComboBox jComboBox20;
    private javax.swing.JComboBox jComboBox21;
    private javax.swing.JComboBox jComboBox22;
    private javax.swing.JComboBox jComboBox23;
    private javax.swing.JComboBox jComboBox24;
    private javax.swing.JComboBox jComboBox25;
    private javax.swing.JComboBox jComboBox3;
    private javax.swing.JComboBox jComboBox4;
    private javax.swing.JComboBox jComboBox5;
    private javax.swing.JComboBox jComboBox6;
    private javax.swing.JComboBox jComboBox7;
    private javax.swing.JComboBox jComboBox8;
    private javax.swing.JComboBox jComboBox9;
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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField10;
    private javax.swing.JTextField jTextField11;
    private javax.swing.JTextField jTextField12;
    private javax.swing.JTextField jTextField13;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    private javax.swing.JButton last10Button;
    private javax.swing.JPanel mainGraphPanel;
    private javax.swing.JButton next10Button;
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
