/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;

import java.util.List;

public class SentenceInfo {
		private String args1;
		private String args2;
		private Entity obj1;
		private Entity obj2;
		private List path1; //container path to entity 1, if applicable
		private List path2; //container path to entity 2, if applicable
		public String getArgs1() 
		{
			return args1;
		}
		public void setArgs1(String args1) 
		{
			this.args1 = args1;
		}
		public String getArgs2() 
		{
			return args2;
		}
		public void setArgs2(String args2) 
		{
			this.args2 = args2;
		}
		public Entity getObj1() 
		{
			return obj1;
		}
		public void setObj1(Entity obj1) 
		{
			this.obj1 = obj1;
		}
		public Entity getObj2() 
		{
			return obj2;
		}
		public void setObj2(Entity obj2) 
		{
			this.obj2 = obj2;
		}
		public List getPath1()
		{
		    return path1;
		}
		public void setPath1(List path1)
		{
		    this.path1 = path1;
		}
		public List getPath2()
		{
		    return path2;
		}
		public void setPath2(List path2)
		{
		    this.path2 = path2;
		}
}
