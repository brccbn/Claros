package org.claros.intouch.notes.controllers;

import java.util.List;

import org.claros.commons.auth.models.AuthProfile;
import org.claros.commons.exception.NoPermissionException;
import org.claros.intouch.common.utility.Constants;
import org.claros.intouch.common.utility.Utility;
import org.claros.intouch.notes.models.Note;

import com.jenkov.mrpersister.impl.mapping.AutoGeneratedColumnsMapper;
import com.jenkov.mrpersister.itf.IGenericDao;
import com.jenkov.mrpersister.itf.mapping.IObjectMappingKey;
import com.jenkov.mrpersister.util.JdbcUtil;

/**
 * @author Umut Gokbayrak
 */
public class NotesController {

	/**
	 * 
	 * @param auth
	 * @return
	 * @throws Exception
	 */
	public static List getUnfiledNotes(AuthProfile auth) throws Exception {
		return getNotesByFolderId(auth, new Long(0));
	}
	
	/**
	 * 
	 * @param auth
	 * @param folderId
	 * @return
	 * @throws Exception
	 */
	public static List getNotesByFolderId(AuthProfile auth, Long folderId) throws Exception {
		IGenericDao dao = null;
		List notes = null;
		try {
			dao = Utility.getDbConnection();
			String username = auth.getUsername();
			
			String sql = "SELECT * FROM NOTES WHERE USERNAME=? AND FOLDER_ID = ? ORDER BY ID DESC";

			notes = dao.readList(Note.class, sql, new Object[] {username, folderId});
		} finally {
			JdbcUtil.close(dao);
			dao = null;
		}
		return notes;
	}

	/**
	 * 
	 * @param auth
	 * @param note
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public static void saveNote(AuthProfile auth, Note note) throws Exception {
		IGenericDao dao = null;
		try {
			dao = Utility.getDbConnection();
			
			Long id = note.getId();
			if (id == null) {
				// it is an insert
				IObjectMappingKey myObj = Constants.persistMan.getObjectMappingFactory().createInstance(Note.class, new AutoGeneratedColumnsMapper(true));
				dao.insert(myObj, note);
			} else {
				// it is an update
				Note tmp = getNoteById(auth, note.getId());
				if (!auth.getUsername().equals(tmp.getUsername())) {
					throw new NoPermissionException();
				}
				dao.update(note);
			}
		} finally {
			JdbcUtil.close(dao);
			dao = null;
		}
	}

	/**
	 * 
	 * @param auth
	 * @param noteId
	 * @throws Exception
	 */
	public static void deleteNote(AuthProfile auth, Long noteId) throws Exception {
		Note tmp = getNoteById(auth, noteId);
		if (!auth.getUsername().equals(tmp.getUsername())) {
			throw new NoPermissionException();
		}

		IGenericDao dao = null;
		try {
			dao = Utility.getDbConnection();
			dao.deleteByPrimaryKey(Note.class, noteId);
		} catch (Exception e) {
			// do nothing sier
		} finally {
			JdbcUtil.close(dao);
			dao = null;
		}
	}

	/**
	 * 
	 * @param auth
	 * @param folderId
	 * @throws Exception
	 */
	public static void deleteNotesByFolderId(AuthProfile auth, Long folderId) throws Exception {
		if (!folderId.equals(new Long(0))) {
			IGenericDao dao = null;
			try {
				dao = Utility.getDbConnection();
				String username = auth.getUsername();
				
				String sql = "DELETE FROM NOTES WHERE USERNAME=? AND FOLDER_ID = ?";

				dao.executeUpdate(sql, new Object[] {username, folderId});
			} catch (Exception e) {
				// do nothing sier
			} finally {
				JdbcUtil.close(dao);
				dao = null;
			}
		}
	}

	/**
	 * 
	 * @param auth
	 * @param noteId
	 * @return
	 * @throws Exception
	 */
	public static Note getNoteById(AuthProfile auth, Long noteId) throws Exception {
		IGenericDao dao = null;
		Note note = null;
		try {
			dao = Utility.getDbConnection();
			String username = auth.getUsername();
			
			String sql = "SELECT * FROM NOTES WHERE USERNAME=? AND ID = ?";
			note = (Note)dao.read(Note.class, sql, new Object[] {username, noteId});
		} finally {
			JdbcUtil.close(dao);
			dao = null;
		}
		return note;
	}
}
