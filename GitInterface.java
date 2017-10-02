/* GitInterface.java � ILS Technology LLC (2017) */
/**
 * 
 */
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.gitrepo.*;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.*;
import org.eclipse.jgit.revwalk.filter.CommitTimeRevFilter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.TreeWalk;

import org.eclipse.jgit.storage.file.FileRepositoryBuilder;



import java.io.File;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author ChristopherFo
 *
 */
public class GitInterface
{
   
	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException, GitAPIException 
	{
		// TODO Auto-generated method stub
        // first create a test-repository, the return is including the .get directory here!

  //      File repoDir = new File("/home/chris/OpenSSL/openssl/.git");
        File repoDir = new File("/home/chris/wildfly-openssl/.git");



        // now open the resulting repository with a FileRepositoryBuilder

        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        try (Repository repository = builder.setGitDir(repoDir)

                .readEnvironment() // scan environment GIT_* variables

                .findGitDir() // scan up the file system tree

                .build()) {

            System.out.println("Repository: " + repository.getDirectory());



            // the Ref holds an ObjectId for any type of object (tree, commit, blob, tree)

            Ref head = repository.exactRef("refs/heads/master");

            System.out.println("exact ref of refs/heads/master: " + head);
            
            ObjectId develop = repository.resolve("develop");
            System.out.println("resolve of \"develop\":"+develop);
            
            Git git = new Git(repository);
            git.checkout().setName("develop").call();
            Iterable<RevCommit> log = git.log().call();
            String k = null;
            for (RevCommit c: log)
            {
            	String dateString;
            	SimpleDateFormat sdf;
            	if (k == null) k=c.name();
            	Date d = new Date(c.getCommitTime() * 1000L);
            	sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            	dateString = sdf.format(d);
            	System.out.println(c.name().substring(0, 8) + "  " + 
            	                   c.getAuthorIdent().getName() + "  " +
            			           new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(c.getCommitTime() * 1000L)) + "  --  " +
            	                   c.getShortMessage());
            }
            Date filterDate = new Date();
            System.out.println("\n --- now try and get the last 48 hours ---\n");
            try
			{
				filterDate = new SimpleDateFormat("yyyy-MM-dd").parse(new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis() 
						- (long) 2 * 24 * 60 * 60 * 1000)));
			}
			catch (ParseException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            log = git.log().setRevFilter(CommitTimeRevFilter.after(filterDate)).call();
            
            for (RevCommit c: log)
            {
            	String dateString;
            	SimpleDateFormat sdf;
            	Date d = new Date(c.getCommitTime() * 1000L);
            	sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            	dateString = sdf.format(d);
            	System.out.println(c.name().substring(0, 8) + "  " + 
            	                   c.getAuthorIdent().getName() + "  " + 
            			           new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(c.getCommitTime() * 1000L)) + "  --  " +
            	                   c.getShortMessage());
            }
            
            System.out.println("\nNow try and find a  commit matching \"0a8143b2\"");
// now find a commit matching "0a8143b2"       
            
            log = git.log().call();
            for (RevCommit c: log)
            {
            	RevTree tree = c.getTree();
            	
            	if (c.name().startsWith("0a8143b2"))
            	{
            		System.out.println("\nCommit Id:" + c.name());
            		System.out.println("\nCommit Author:" + c.getCommitterIdent().getName());
            		System.out.println("Commit Author email:" + c.getCommitterIdent().getEmailAddress());
            		System.out.println("Commit Time: "+ c.getCommitTime());
            		System.out.println("Author :" + c.getAuthorIdent().getName());
            		System.out.println("Author Email:" + c.getAuthorIdent().getEmailAddress());
            		System.out.println("\n>>>>>>>"+c.getShortMessage()+"\n");
            		System.out.println("==============\n" + c.getFullMessage()
            		                  +"==============\n");
            		System.out.println("Tree: " + tree + "\n Now walk it to get files!\n");
            		TreeWalk treeWalk = new TreeWalk(repository);
            		treeWalk.addTree(tree);  // walk the tree
            		treeWalk.setRecursive(false);  // true - all files in tree false - top level only
            		while (treeWalk.next())
            			System.out.println("File: " + treeWalk.getPathString());  // print files
            		break;
            	}
            }
        }
	}

}
