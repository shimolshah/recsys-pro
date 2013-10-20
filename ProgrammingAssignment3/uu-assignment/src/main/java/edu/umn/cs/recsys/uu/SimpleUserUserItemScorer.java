package edu.umn.cs.recsys.uu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import it.unimi.dsi.fastutil.longs.LongSet;

import org.grouplens.lenskit.basic.AbstractItemScorer;
import org.grouplens.lenskit.data.dao.ItemEventDAO;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.history.History;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.scored.PackedScoredIdList;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.scored.ScoredIdListBuilder;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.grouplens.lenskit.vectors.similarity.CosineVectorSimilarity;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * User-user item scorer.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SimpleUserUserItemScorer extends AbstractItemScorer {
    private final UserEventDAO userDao;
    private final ItemEventDAO itemDao;

    @Inject
    public SimpleUserUserItemScorer(UserEventDAO udao, ItemEventDAO idao) {
        userDao = udao;
        itemDao = idao;
    }

    @Override
    public void score(long user, @Nonnull MutableSparseVector scores) {
        SparseVector userVector = getUserRatingVector(user);

        double user_rating_mean = userVector.mean();
        
        System.out.println("---");
        for (long item : scores.keyDomain())
        {
        	//System.out.println("Item: " + item);

            List<ScoredId> neighbors = findTop30neighbors(user, item, userVector);
            
            double num = 0;
            double denom = 0;
            for (ScoredId i : neighbors )
            {
            	SparseVector neighbor_vector = getUserRatingVector(i.getId());
            	//System.out.println("neighbor: " + i.getId() + " score: " + i.getScore());
            	num += i.getScore() * (neighbor_vector.get(item) - neighbor_vector.mean());
            	denom += Math.abs(i.getScore());
            }
            double score = user_rating_mean + num/denom;
            //System.out.println("Score: " + score);
            scores.set(item, score);
        }
        System.out.println("---");
        
    }

    /**
     * Get a user's rating vector.
     * @param user The user ID.
     * @return The rating vector.
     */
    private SparseVector getUserRatingVector(long user) {
        UserHistory<Rating> history = userDao.getEventsForUser(user, Rating.class);
        if (history == null) {
            history = History.forUser(user);
        }
        return RatingVectorUserHistorySummarizer.makeRatingVector(history);
    }
    
    List<ScoredId> findTop30neighbors(long user, long item, SparseVector userVector)
    {
        CosineVectorSimilarity cvs = new CosineVectorSimilarity();
    	ScoredIdListBuilder slb = new ScoredIdListBuilder();

    	LongSet users = itemDao.getUsersForItem(item);

    	for(long j : users)
        {
        	if(j != user) //Skip the current user from neighbor finding calculation
        	{
        		SparseVector jVector = getUserRatingVector(j);
        		
        		MutableSparseVector mutable_jVector = jVector.mutableCopy();
        		MutableSparseVector mutable_userVector = userVector.mutableCopy();
        		mutable_jVector.add(-1 * jVector.mean());
        		mutable_userVector.add(-1 * userVector.mean());
        		double similarity = cvs.similarity(mutable_userVector.freeze(), mutable_jVector.freeze());
        		slb.add(j,similarity);
        	}
        }
        PackedScoredIdList psil = slb.sort(new Comparator<ScoredId> (){
        	public int compare(ScoredId s1, ScoredId s2) 
        	{
        		double score1 = s1.getScore();
        		double score2 = s2.getScore();
        		if (score1 > score2)
        			return -1;
        		else if(score1 == score2)
        			return 0;
        		else
        			return 1;
        	}
        }).finish();
        
        List<ScoredId> list = psil.subList(0, 30);
        return list;
    }
}
