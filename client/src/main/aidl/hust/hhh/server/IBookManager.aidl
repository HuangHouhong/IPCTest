// IBookManager.aidl
package hust.hhh.server;

import hust.hhh.server.Book;
import hust.hhh.server.IOnNewBookArrivedListener;


interface IBookManager {
   List<Book> getBookList();
   void addBook(in Book book);
   void registerListener(in IOnNewBookArrivedListener listener);
   void unRegisterListener(in IOnNewBookArrivedListener listener);
}
