package featurecat.lizzie.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.UnsupportedLookAndFeelException;

import org.json.JSONException;
import org.junit.Test;

import featurecat.lizzie.Config;
import featurecat.lizzie.Lizzie;
import featurecat.lizzie.analysis.Leelaz;
import featurecat.lizzie.analysis.MoveData;
import featurecat.lizzie.gui.LizzieFrame;

public class SGFParserTest {
    
    private static ArrayList<Integer> laneUsageList = new ArrayList<Integer>();
    private static List<String> moveList = new ArrayList<String>();

    @Test
    public void testVariaionOnly1() throws IOException, JSONException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, UnsupportedLookAndFeelException, InterruptedException {
        
        String sgfString = "(;B[pd];W[dp];B[pp];W[dd];B[fq]"
                + "(;W[cn];B[cc];W[cd];B[dc];W[ed];B[fc];W[fd]"
                + "(;B[gb]"
                + "(;W[hc];B[nq])"
                + "(;W[gc];B[ec];W[hc];B[hb];W[ic]))"
                + "(;B[gc];W[ec];B[eb];W[fb];B[db];W[hc];B[gb];W[gd];B[hb]))"
                + "(;W[nq];B[cn];W[fp];B[gp];W[fo];B[dq];W[cq];B[eq];W[cp];B[dm];W[fm]))";
        
        int variationNum = 4;
        String mainBranch = ";B[pd];W[dp];B[pp];W[dd];B[fq]";

        Lizzie lizzie = new Lizzie();
        lizzie.config = new Config();
        lizzie.board = new Board();
        lizzie.frame = new LizzieFrame();
        // new Thread( () -> {
        try {
            lizzie.leelaz = new Leelaz();
            // lizzie.leelaz.togglePonder();
        } catch (JSONException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // }).start();

        // Load correctly
        boolean loaded = SGFParser.loadFromString(sgfString);
        assertTrue(loaded);

        // Save correctly
        String saveSgf = SGFParser.saveToString();
        assertTrue(saveSgf != null && saveSgf.trim().length() > 0);

        String gameInfo = String.format("(?s).*AP\\[Lizzie: %s\\]", Lizzie.lizzieVersion);
        saveSgf = saveSgf.replaceFirst(gameInfo, "(");
        assertEquals(sgfString, saveSgf);

        getVariationTree(0, lizzie.board.getHistory().getCurrentHistoryNode(), 0, true);

        List<String> list = this.moveList;
    }
    
    /**
     * Get Variation Tree as String List
     * The logic is same as the function VariationTree.drawTree
     * 
     * @param startLane
     * @param startNode
     * @param variationNumber
     * @param isMain
     */
    public static void getVariationTree(int startLane, BoardHistoryNode startNode, int variationNumber, boolean isMain) {
        // Finds depth on leftmost variation of this tree
        int depth = BoardHistoryList.getDepth(startNode) + 1;
        int lane = startLane;
        // Figures out how far out too the right (which lane) we have to go not to collide with other variations
        while (lane < laneUsageList.size() && laneUsageList.get(lane) <= startNode.getData().moveNumber + depth) {
            // laneUsageList keeps a list of how far down it is to a variation in the different "lanes"
            laneUsageList.set(lane, startNode.getData().moveNumber - 1);
            lane++;
        }
        if (lane >= laneUsageList.size())
        {
                laneUsageList.add(0);
        }
        if (variationNumber > 1)
            laneUsageList.set(lane - 1, startNode.getData().moveNumber - 1);
        laneUsageList.set(lane, startNode.getData().moveNumber);

        // At this point, lane contains the lane we should use (the main branch is in lane 0)
        BoardHistoryNode cur  = startNode;

        // Draw main line
        StringBuilder sb = new StringBuilder();
        sb.append(formatMove(cur.getData()));
        while (cur.next() != null) {
            cur = cur.next();
            sb.append(formatMove(cur.getData()));
        }
        moveList.add(sb.toString());
        // Now we have drawn all the nodes in this variation, and has reached the bottom of this variation
        // Move back up, and for each, draw any variations we find
        while (cur.previous() != null && cur != startNode) {
            cur = cur.previous();
            int curwidth = lane;
            // Draw each variation, uses recursion
            for (int i = 1; i < cur.numberOfChildren(); i++) {
                curwidth++;
                // Recursion, depth of recursion will normally not be very deep (one recursion level for every variation that has a variation (sort of))
                getVariationTree(curwidth, cur.getVariation(i), i, false);
            }
        }
    }
    
    private static String formatMove(BoardData data) {
        String stone = "";
        if (Stone.BLACK.equals(data.lastMoveColor)) stone = "B";
        else if (Stone.WHITE.equals(data.lastMoveColor)) stone = "W";
        else return stone;

        char x = data.lastMove == null ? 't' : (char) (data.lastMove[0] + 'a');
        char y = data.lastMove == null ? 't' : (char) (data.lastMove[1] + 'a');

        return String.format(";%s[%c%c]", stone, x, y);
    }

}
