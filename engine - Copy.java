//DEFAULT IMPORTS
import java.util.*;

//DISPLAY IMPORTS
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
//vbo specific
import org.lwjgl.opengl.ARBBufferObject;
import org.lwjgl.opengl.ARBVertexBufferObject;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.GLU;

//INPUT IMPORTS
import org.lwjgl.input.Mouse;
import org.lwjgl.input.Keyboard;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Component;
import net.java.games.input.Controller;

//3D MATH IMPORTS
import javax.vecmath.Vector3f;

//TEXTURE IMPORTS
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

//TEXTURE MANIPULATION
import javax.imageio.*;
import java.awt.Image;
import java.awt.image.*;
import java.awt.Color;
import java.awt.Graphics2D;

//Physics
import com.bulletphysics.BulletGlobals;
import com.bulletphysics.BulletStats;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.shapes.*;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.Point2PointConstraint;
import com.bulletphysics.dynamics.constraintsolver.TypedConstraint;
import com.bulletphysics.linearmath.convexhull.*;
import com.bulletphysics.linearmath.CProfileIterator;
import com.bulletphysics.linearmath.CProfileManager;
import com.bulletphysics.linearmath.Clock;
import com.bulletphysics.linearmath.DebugDrawModes;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.QuaternionUtil;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.linearmath.VectorUtil;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import com.bulletphysics.demos.opengl.*;
import com.bulletphysics.util.ObjectPool;
import com.bulletphysics.util.IntArrayList;
import org.lwjgl.opengl.GL11;
import com.bulletphysics.collision.broadphase.AxisSweep3;
import java.util.ArrayList;
import java.util.List;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.broadphase.BroadphaseNativeType;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CylinderShape;
import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.StaticPlaneShape;
import com.bulletphysics.collision.shapes.ShapeHull;
import com.bulletphysics.demos.opengl.DemoApplication;
import com.bulletphysics.demos.opengl.GLDebugDrawer;
import com.bulletphysics.demos.opengl.IGL;
import com.bulletphysics.demos.opengl.LWJGL;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import org.lwjgl.LWJGLException;

public class engine{

	public static void main(String args[]){
		new engine();
	}

	public engine(){
		debug("Beginning P");
		new p();//builds my triangle vbo to be sent to my video thread, no other classes can touch the tmpTriangle or triangle arraylist
		debug("Beginning V");
		new v();
		debug("Beginning M");
		new m();
		debug("Beginning K");
		new k();
		
		debug("Beginning New Shape");
		new pShapeDesigner();

		//utility loader methods now
		debug("Beginning Tri Fill Buffer");
		v.vFillTriDemo();

		//add interface
		//p.p3dRuler();

		mScreen = 2;
	}
	
	public void debug(String message)
	{
		if(debug)System.out.println(message);
	}
	
	boolean debug = true;

	//All display windows are linked by a "/example" identifier.

	public class p implements Runnable{
		public p(){(new Thread(this)).start();p = this;}
		public void run(){
			try{
				pCollisionConfiguration = new DefaultCollisionConfiguration();
				pInterfaceCollisionConfiguration = new DefaultCollisionConfiguration();
				pDispatcher = new CollisionDispatcher(pCollisionConfiguration);
				pInterfaceDispatcher = new CollisionDispatcher(pInterfaceCollisionConfiguration);
				Vector3f pWorldMin = new Vector3f(-1000f,-1000f,-1000f);
				Vector3f pWorldMax = new Vector3f(1000f,1000f,1000f);
				BroadphaseInterface pBroadphase = new AxisSweep3(pWorldMin, pWorldMax);
				BroadphaseInterface pInterfaceBroadphase = new AxisSweep3(pWorldMin, pWorldMax);
				pSolver = new SequentialImpulseConstraintSolver();
				pInterfaceSolver = new SequentialImpulseConstraintSolver();
				pDynamicsWorld = new DiscreteDynamicsWorld(pDispatcher,pBroadphase,pSolver,pCollisionConfiguration);
				pInterfaceWorld = new DiscreteDynamicsWorld(pInterfaceDispatcher,pInterfaceBroadphase,pInterfaceSolver,pInterfaceCollisionConfiguration);
				//Vector3f pGravity = new Vector3f(new Vector3f(0f, -1f, 0f));
				//pDynamicsWorld.setGravity(pGravity);

				float fps = 100;
				float fpsAmount = 1f/fps;
				int pollSpeed = (int)(fpsAmount * 1000);
				int physicsDelay = 10;
				long pDeltaTime = System.nanoTime();

				while(true){
					while(pDynamicsWorld != null) {
						if(pThreadPausedHint){
							pThreadPausedHint = false;
							pThreadPaused = true;
							while(pThreadPaused){
								uSleep(1);
							}
						}
						else{
							try{
								pElapsedTime1 = System.nanoTime()-pDeltaTime;
								pElapsedTime1 /= pNano;
								pDeltaTime = System.nanoTime();
								pDynamicsWorld.stepSimulation(pElapsedTime1,1,1f/fps);
							}catch(Exception e){System.out.println("PHYSICS " + e);}
							pUpdateVT();//System.out.println("UPDATE VBO");
						}

						uSleep(physicsDelay);
					}
					uSleep(1000);
				}
			}catch(Exception e){}
		}

		public void pUpdateVT(){
			vTmpTriangleList = new ArrayList();
			for(int x = 0; x < pShapeList.size();x++){
				CollisionObject co = (CollisionObject) pShapeList.get(x);
				CollisionShape shape = co.getCollisionShape();
				RigidBody tmpBody = (RigidBody) pRigid.get(x);
				Transform tmpTransform = new Transform();
				if(tmpBody!=null){
					DefaultMotionState ms = (DefaultMotionState) tmpBody.getMotionState();
					ms.getWorldTransform(tmpTransform);
				}
				//System.out.println(tmpTransform.origin.x + " " + tmpTransform.origin.y + " " + tmpTransform.origin.z);

				//Build new triangle arraylist and then swap out pointer

				if(v!=null && vTriangles!=null){
					if(pHullResultList.size()>x){
						pHullShape tmpHull = (pHullShape) pHullResultList.get(x);

						IntArrayList idx = tmpHull.idx;
						List<Vector3f> vtx = tmpHull.vtx;
						List<Vector3f> vtx2 = new ArrayList();

						Matrix3f tmpMat = new Matrix3f();
						tmpMat.set(tmpTransform.basis);//rot set

						for(int y = 0; y < vtx.size(); y++){//set positioning and rotation
							Vector3f tmpVtx = new Vector3f(vtx.get(y));
							tmpMat.transform(tmpVtx);
							tmpVtx.set(mathUtil.add(tmpVtx,tmpTransform.origin));
							vtx2.add(tmpVtx);
						}

						for(int y = 0; y < idx.size()/3; y++){//x = triangles idx.size/3 is the max amount
							vTriangle tmpTriangle = new vTriangle(new Vector3f(vtx2.get(idx.get(y*3+0))), new Vector3f(0,0,0),new Vector3f(vtx2.get(idx.get(y*3+1))),new Vector3f(0,1,0),new Vector3f(vtx2.get(idx.get(y*3+2))),new Vector3f(1,0,0));
							vTmpTriangleList.add(tmpTriangle);
							//v.addVTriangle(new Vector3f(vtx.get(idx.get(x*3+0))), new Vector3f(0,0,0),new Vector3f(vtx.get(idx.get(x*3+1))),new Vector3f(0,1,0),new Vector3f(vtx.get(idx.get(x*3+2))),new Vector3f(1,0,0));
						}
					}

					else if(pHullResultList.size()+pTriangles.size()>x){
						vTriangle tmpPTriangle = (vTriangle) pTriangles.get(x-pHullResultList.size());
						Matrix3f tmpMat = new Matrix3f();
						tmpMat.set(tmpTransform.basis);//rot set

						List<Vector3f> vtx = new ArrayList();
						for(int y = 0; y < 3; y++){//set positioning and rotation
							Vector3f tmpVtx1 = new Vector3f(tmpPTriangle.v1);
							Vector3f tmpVtx2 = new Vector3f(tmpPTriangle.v2);
							Vector3f tmpVtx3 = new Vector3f(tmpPTriangle.v3);
							tmpMat.transform(tmpVtx1);
							tmpMat.transform(tmpVtx2);
							tmpMat.transform(tmpVtx3);
							tmpVtx1.set(mathUtil.add(tmpVtx1,tmpTransform.origin));
							tmpVtx2.set(mathUtil.add(tmpVtx2,tmpTransform.origin));
							tmpVtx3.set(mathUtil.add(tmpVtx3,tmpTransform.origin));
							vtx.add(tmpVtx1);
							vtx.add(tmpVtx2);
							vtx.add(tmpVtx3);
						}
						vTriangle tmpTriangle = new vTriangle(new Vector3f(vtx.get(0)), textureUtil.c(0,0),new Vector3f(vtx.get(1)),textureUtil.c(0,1),new Vector3f(vtx.get(2)),textureUtil.c(0,2));
						vTmpTriangleList.add(tmpTriangle);
						//v.ad
					}
				}
			}
			v.vFillOtherTriangleObjects();
			vTriangles = vTmpTriangleList;
			vRefreshVbo = true;
		}

		int shapeCounter;

		public void p3dRuler(){
			float length = 10/2f;
			float width = 10/2f;
			List<Vector3f> vertices = new ArrayList<Vector3f>();
			vertices.add(new Vector3f(-length,0,-width));
			vertices.add(new Vector3f(-length,0,width));
			vertices.add(new Vector3f(length,0,width));
			vertices.add(new Vector3f(length,0,-width));
			Transform startTransform = new Transform();
			startTransform.setIdentity();
			Vector3f translate = new Vector3f();
			startTransform.origin.set(translate);
			CollisionShape shape = new ConvexHullShape(vertices);
			Vector3f localInertia = new Vector3f(0f, 0f, 0f);
			DefaultMotionState myMotionState = new DefaultMotionState(startTransform);
			RigidBodyConstructionInfo cInfo = new RigidBodyConstructionInfo(0f, myMotionState, shape, localInertia);
			RigidBody body = new RigidBody(cInfo);
			body.setGravity(new Vector3f(0,0,0));
			pInterfaceWorld.addRigidBody(body);
			//adding this to another dynamic world*interfaceworld*
		}

		public void pVadd3dRuler(){
			float length = 10/2f;
			float width = 10/2f;
			Vector3f v1 = new Vector3f(-length,0,-width);
			Vector3f v2 = new Vector3f(-length,0,width);
			Vector3f v3 = new Vector3f(length,0,width);
			Vector3f v4 = new Vector3f(length,0,-width);
			//top left half of letter
			vTriangle tmpTriangle1 = new vTriangle(v3, new Vector3f(0,0,0),
													v1, new Vector3f(1,0,0),
														v4,new Vector3f(1,1,0));

			//bottom right half of letter
			vTriangle tmpTriangle2 = new vTriangle(v1, new Vector3f(0,0,0),
													v3, new Vector3f(1,0,0),
														v2,new Vector3f(1,1,0));
			vTmpTriangleList.add(tmpTriangle1);
			vTmpTriangleList.add(tmpTriangle2);
		}

		public void pAddT(List<Vector3f> vertices, Vector3f translate, float mass){
			//System.out.println(shapeCounter++);
			boolean failSafeDontAdd = false;

			pThreadPausedHint = true;
			vThreadPausedHint = true;
			while(vThreadPausedHint || pThreadPausedHint){
				uSleep(1);
			}

			Transform startTransform = new Transform();
			startTransform.setIdentity();
			startTransform.origin.set(translate);
			CollisionShape shape=null;
			try{
				shape = new ConvexHullShape(vertices);
			}catch(Exception e){
				failSafeDontAdd = true;
				System.out.println("FAILED TO MAKE CONVEXHULL: " + e);
			}
			if(shape == null)System.out.println("SHAPE: IM NULL");

			if(!failSafeDontAdd){
				CollisionObject co = new CollisionObject();
				co.setCollisionShape(shape);
				Vector3f localInertia = new Vector3f(0f, 0f, 0f);
				boolean isDynamic = (mass != 0f);
				if (isDynamic) {
					shape.calculateLocalInertia(mass, localInertia);
				}
				ConvexShape convexShape = (ConvexShape)shape;
				ShapeHull hull = new ShapeHull(convexShape);

				HullDesc hullD = new HullDesc(HullFlags.TRIANGLES, vertices.size(), vertices);
				HullResult hullR = new HullResult();
				HullLibrary hl = new HullLibrary();

				try{
					hl.createConvexHull(hullD, hullR);
					pHullResultList.add(new pHullShape(hullR));
					IntArrayList idx = hullR.indices;
					List<Vector3f> vtx = hullR.outputVertices;
					for(int x = 0; x < idx.size()/3; x++){//x = triangles idx.size/3 is the max amount
						vTriangle tmpTriangle = new vTriangle(new Vector3f(vtx.get(idx.get(x*3+0))), new Vector3f(0,0,0),new Vector3f(vtx.get(idx.get(x*3+1))),new Vector3f(0,1,0),new Vector3f(vtx.get(idx.get(x*3+2))),new Vector3f(1,0,0));
						vTriangles.add(tmpTriangle);
					}
					hl.releaseResult(hullR);

					DefaultMotionState myMotionState = new DefaultMotionState(startTransform);
					RigidBodyConstructionInfo cInfo = new RigidBodyConstructionInfo(mass, myMotionState, shape, localInertia);
					RigidBody body = new RigidBody(cInfo);
					if(mass == 0)body.setGravity(new Vector3f(0,0,0));
					//body.setGravity(new Vector3f(0,0,0));
					body.setActivationState(1);
					body.setFriction(1f);
					body.activate();
					pRigid.add(body);
					pDynamicsWorld.addRigidBody(body);
					pShapeList.add(co);
					vRefreshVbo = true;
				}catch(Exception e){
					failSafeDontAdd = true;
					System.out.println("HULL FAILED: " + e);
					for(int x = 0; x < vertices.size(); x++){
						Vector3f tmpVect = vertices.get(x);
						System.out.println(tmpVect.x + " " + tmpVect.y + " " + tmpVect.z);
					}
				}
			}
			//v.vTriFillVbo();
			vThreadPaused = false;
			pThreadPaused = false;
		}

		public void pAddTriag(List<Vector3f> vertices, Vector3f translate, float mass){
			//System.out.println(shapeCounter++);

			pThreadPausedHint = true;
			vThreadPausedHint = true;
			while(vThreadPausedHint || pThreadPausedHint){
				uSleep(1);
			}

			Transform startTransform = new Transform();
			startTransform.setIdentity();
			startTransform.origin.set(translate);
			CollisionShape shape=null;
			try{
				shape = new ConvexHullShape(vertices);
			}catch(Exception e){
			}

			CollisionObject co = new CollisionObject();
			co.setCollisionShape(shape);
			Vector3f localInertia = new Vector3f(0f, 0f, 0f);
			boolean isDynamic = (mass != 0f);
			if (isDynamic) {
				shape.calculateLocalInertia(mass, localInertia);
			}
			ConvexShape convexShape = (ConvexShape)shape;

			try{
				vTriangle tmpTriangle = new vTriangle(new Vector3f(vertices.get(0)), textureUtil.c(0,0),new Vector3f(vertices.get(1)),textureUtil.c(0,1),new Vector3f(vertices.get(2)),textureUtil.c(0,2));
				vTriangle tmpPTriangle = new vTriangle(new Vector3f(vertices.get(0)), new Vector3f(0,0,0),new Vector3f(vertices.get(1)),new Vector3f(0,1,0),new Vector3f(vertices.get(2)),new Vector3f(1,0,0));
				/*vTriangle tmpTriangle = new vTriangle(new Vector3f(vertices.get(0)), new Vector3f(0,0,0),new Vector3f(vertices.get(1)),new Vector3f(0,1,0),new Vector3f(vertices.get(2)),new Vector3f(1,0,0));
				vTriangle tmpPTriangle = new vTriangle(new Vector3f(vertices.get(0)), new Vector3f(0,0,0),new Vector3f(vertices.get(1)),new Vector3f(0,1,0),new Vector3f(vertices.get(2)),new Vector3f(1,0,0));*/
				vTriangles.add(tmpTriangle);
				pTriangles.add(tmpPTriangle);

				DefaultMotionState myMotionState = new DefaultMotionState(startTransform);
				RigidBodyConstructionInfo cInfo = new RigidBodyConstructionInfo(mass, myMotionState, shape, localInertia);
				RigidBody body = new RigidBody(cInfo);
				if(mass == 0)body.setGravity(new Vector3f(0,0,0));
				//body.setGravity(new Vector3f(0,0,0));
				body.setActivationState(1);
				body.setFriction(1f);
				body.activate();
				pRigid.add(body);
				pDynamicsWorld.addRigidBody(body);
				pShapeList.add(co);
				vRefreshVbo = true;
			}catch(Exception e){}
			//v.vTriFillVbo();
			vThreadPaused = false;
			pThreadPaused = false;
		}

		public boolean pVectorClickTest(){

			while(pEVThreadPaused){
				uSleep(1);
			}

			mEVThreadPaused = true;

			boolean test = false;
			ArrayList tmpRigidBodies = new ArrayList();

			for(int x = 0; x < editorVertices.size();x++){
				Vector3f tmpVector = (Vector3f) editorVertices.get(x);
				float[] phiTheta = mathUtil.phiTheta(tmpVector,cameraUtil.position());
				float height = 1f/2f;
				float width = 1f/2f;
				Vector3f v1 = mathUtil.step(phiTheta[0]+90f,0f,width);
				v1 = mathUtil.add(v1,tmpVector);
				Vector3f v2 = mathUtil.step(phiTheta[0]-90f,0f,width);
				v2 = mathUtil.add(v2,tmpVector);
				Vector3f v3 = mathUtil.clone(v1);
				Vector3f v4 = mathUtil.clone(v2);
				v1.y+=height;
				v2.y+=height;
				v3.y-=height;
				v4.y-=height;

				List<Vector3f> vertices = new ArrayList<Vector3f>();
				vertices.add(v1);
				vertices.add(v2);
				vertices.add(v3);
				vertices.add(v4);

				Transform startTransform = new Transform();
				startTransform.setIdentity();
				Vector3f translate = new Vector3f();
				startTransform.origin.set(translate);
				CollisionShape shape = new ConvexHullShape(vertices);
				Vector3f localInertia = new Vector3f(0f, 0f, 0f);
				DefaultMotionState myMotionState = new DefaultMotionState(startTransform);
				RigidBodyConstructionInfo cInfo = new RigidBodyConstructionInfo(0f, myMotionState, shape, localInertia);
				RigidBody body = new RigidBody(cInfo);
				body.setGravity(new Vector3f(0,0,0));
				pInterfaceWorld.addRigidBody(body);
				tmpRigidBodies.add(body);
			}

			Vector3f v1 = cameraUtil.position();
			Vector3f v2 = mathUtil.step(vCameraAngle, vCameraLook, 100);
			v2 = mathUtil.add(v1,v2);

			CollisionWorld.ClosestRayResultCallback resultCallback = new CollisionWorld.ClosestRayResultCallback(v1, v2);
			pInterfaceWorld.rayTest(v1, v2, resultCallback);
			if (resultCallback.hasHit()){
				RigidBody tmpBody = RigidBody.upcast(resultCallback.collisionObject);
				Vector3f hitspot = new Vector3f(resultCallback.hitPointWorld);
				//System.out.println("HIT " + hitspot.x + " " + hitspot.y + " " + hitspot.z);
				for(int x = 0; x<tmpRigidBodies.size();x++){
					if(tmpBody == (RigidBody)tmpRigidBodies.get(x)){
						//System.out.println(x + " vertice hit");
						editorVectorClicked = x;
						test = true;
					}
				}
			}else{
				editorVectorClicked = -1;
			}

			for(int x = 0; x<tmpRigidBodies.size();x++){
				pInterfaceWorld.removeRigidBody((RigidBody)tmpRigidBodies.get(x));
			}
			mEVThreadPaused = false;
			return test;
		}

		public void pRightClick(){
			Vector3f me = new Vector3f(vCameraX,vCameraY,vCameraZ);
			Vector3f step1 = mathUtil.step(vCameraAngle, vCameraLook, 100);
			Vector3f step2 = mathUtil.add(step1, me);
			CollisionWorld.ClosestRayResultCallback resultCallback = new CollisionWorld.ClosestRayResultCallback(me, step2);
			pInterfaceWorld.rayTest(me, step2, resultCallback);
			if (resultCallback.hasHit()){
				RigidBody tmpBody = RigidBody.upcast(resultCallback.collisionObject);
				Vector3f hitspot = new Vector3f(resultCallback.hitPointWorld);
				//System.out.println("HIT " + hitspot.x + " " + hitspot.y + " " + hitspot.z);
			}else{
				//System.out.println("NOT HIT");
			}
		}
	}

 	public class v implements Runnable{
		public v(){
			debug("Starting new thread");
			(new Thread(this)).start();v = this;}

		public void run(){
			debug("In the run block");
			try{
				debug("Setting Full Screen");
				Display.setFullscreen(vFullscreen);
				debug("Getting Available Display Modes");
		        DisplayMode d[] = Display.getAvailableDisplayModes();
		        for (int i = 0; i < d.length; i++) {
		        	debug("Possible mode: w[" + d[i].getWidth() + "] h[" + d[i].getHeight() + "] pixel depth["+d[i].getBitsPerPixel()+"]");
		            if (d[i].getWidth() == vScreenW
		                && d[i].getHeight() == vScreenH
		                && d[i].getBitsPerPixel() == vDpi) {
		                vDisplayMode = d[i];
		                break;
		            }
		        }
		        Display.setDisplayMode(vDisplayMode);
		        Display.setLocation(0,0);
		        Display.setTitle(vTitle);
		        Display.create();
			}catch(Exception e){debug("Video Exception: " + e);}

			//initialize basic viewport
			GL11.glMatrixMode(GL11.GL_PROJECTION);
        	GL11.glLoadIdentity();
        	GLU.gluPerspective(45.0f,(float) vDisplayMode.getWidth() / (float) vDisplayMode.getHeight(),0.1f,500.0f);
        	GL11.glMatrixMode(GL11.GL_MODELVIEW);

        	GL11.glDisable(GL11.GL_BLEND);
        	GL11.glEnable(GL11.GL_DEPTH_TEST);

        	GL11.glClearColor(0.18f, .18f, 1f, 0.0f);
        	//GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);


        	//initialize vbo portion
			if (!GLContext.getCapabilities().GL_ARB_vertex_buffer_object){
				System.out.println("ARB VBO not supported!");
				System.exit(1);
			}
			IntBuffer vboInt_buffer = ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder()).asIntBuffer();
			ARBBufferObject.glGenBuffersARB(vboInt_buffer);
			vboBuffer_id = vboInt_buffer.get(0);
			vboIndices_buffer_id = vboInt_buffer.get(1);
			ARBBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, vboBuffer_id);
			ARBBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, vboIndices_buffer_id);
			GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
			GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
			//TIP: int stride = (3 + 3 + 4 + 2) * 4; // 3 for vertex, 3 for normal, 4 for colour and 2 for texture coordinates. * 4 for bytes
			GL11.glVertexPointer(3, GL11.GL_FLOAT, (3+2)*4, 0);
			GL11.glTexCoordPointer(2, GL11.GL_FLOAT, (3+2)*4, (3)*4);
			//initialize textures
			textureUtil.map.add("images/minus.bmp");
			textureUtil.map.add("images/minus.bmp");
			//textureUtil.map.add("images/plus.bmp");
			textureUtil.map.add("images/fontTest.bmp");
			//TEST TEST TEST DEMO RUN
			//run from engine()
			//vFillTriDemo();
			GL11.glEnable(GL11.GL_TEXTURE_2D);

			textureUtil.addT("PNG", "textureMap.png");
			textureUtil.bind(0);

			//test shading
			//GL11.glDisable(GL11.GL_DEPTH_TEST);
        	//GL11.glEnable(GL11.GL_BLEND);

        	GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);       // Select The Type Of Blending

			while(true){
				vFrameCount++;
				//vFrameCount %= 100;
				if (!Display.isVisible())uSleep(100);
				else if(Display.isCloseRequested())System.exit(0);
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
                GL11.glLoadIdentity();
		        GL11.glRotatef(vCameraLook,1.0f,0,0);
		        GL11.glRotatef(360.0f - vCameraAngle,0,1.0f,0);
		        GL11.glTranslatef(-vCameraX, -vCameraY, -vCameraZ);

				//vTriFillVbo();

				if(vVBOSETONCE){
					vLineGrid();
					vTriFillVbo();
					//triangle
					//GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
					GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
					//TIP: int stride = (3 + 3 + 4 + 2) * 4; // 3 for vertex, 3 for normal, 4 for colour and 2 for texture coordinates. * 4 for bytes
					GL11.glVertexPointer(3, GL11.GL_FLOAT, (3+2)*4, 0);
					GL11.glTexCoordPointer(2, GL11.GL_FLOAT, (3+2)*4, (3)*4);
					ByteBuffer new_mapped_buffer = ARBBufferObject.glMapBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB,ARBBufferObject.GL_WRITE_ONLY_ARB,vboMapped_buffer);
					if (new_mapped_buffer != vboMapped_buffer)vboMapped_float_buffer = new_mapped_buffer.order(ByteOrder.nativeOrder()).asFloatBuffer();
					vboMapped_buffer = new_mapped_buffer;
					new_mapped_buffer = ARBBufferObject.glMapBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB,ARBBufferObject.GL_WRITE_ONLY_ARB,vboMapped_indices_buffer);
					if (new_mapped_buffer != vboMapped_indices_buffer)vboMapped_indices_int_buffer = new_mapped_buffer.order(ByteOrder.nativeOrder()).asIntBuffer();
					vboMapped_float_buffer.rewind();
					vboVertices.rewind();
					vboMapped_float_buffer.put(vboVertices);
					vboMapped_indices_int_buffer.rewind();
					vboIndices.rewind();
					vboMapped_indices_int_buffer.put(vboIndices);
					if (ARBBufferObject.glUnmapBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB) && ARBBufferObject.glUnmapBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB)){
						GL11.glDrawElements(GL11.GL_TRIANGLES, vTriangles.size()*3, GL11.GL_UNSIGNED_INT, 0);
					}


					//lines
					//GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
					GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
					//TIP: int stride = (3 + 3 + 4 + 2) * 4; // 3 for vertex, 3 for normal, 4 for colour and 2 for texture coordinates. * 4 for bytes
					GL11.glVertexPointer(3, GL11.GL_FLOAT,0, 0);
					vLineFillVbo();
					//GL11.glTexCoordPointer(2, GL11.GL_FLOAT, (3+2)*4, (3)*4);
					ByteBuffer new_Linemapped_buffer = ARBBufferObject.glMapBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB,ARBBufferObject.GL_WRITE_ONLY_ARB,vboLineMapped_buffer);
					if (new_Linemapped_buffer != vboLineMapped_buffer)vboLineMapped_float_buffer = new_Linemapped_buffer.order(ByteOrder.nativeOrder()).asFloatBuffer();
					vboLineMapped_buffer = new_Linemapped_buffer;
					new_Linemapped_buffer = ARBBufferObject.glMapBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB,ARBBufferObject.GL_WRITE_ONLY_ARB,vboLineMapped_indices_buffer);
					if (new_Linemapped_buffer != vboLineMapped_indices_buffer)vboLineMapped_indices_int_buffer = new_Linemapped_buffer.order(ByteOrder.nativeOrder()).asIntBuffer();
					vboLineMapped_float_buffer.rewind();
					vboLineVertices.rewind();
					vboLineMapped_float_buffer.put(vboLineVertices);
					vboLineMapped_indices_int_buffer.rewind();
					vboLineIndices.rewind();
					vboLineMapped_indices_int_buffer.put(vboLineIndices);
					if (ARBBufferObject.glUnmapBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB) && ARBBufferObject.glUnmapBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB)){
						GL11.glDrawElements(GL11.GL_LINES, vLines.size()*2, GL11.GL_UNSIGNED_INT, 0);
					}
				}
				Display.update();
				uSleep(1);

				if(vThreadPausedHint){
					vThreadPausedHint = false;
					vThreadPaused = true;
					while(vThreadPaused){
						uSleep(1);
					}
				}
				else{
					if(vRefreshVbo){
						vTriFillVbo();
						//vLineFillVbo();
						vRefreshVbo = false;
					}
				}
			}
		}

		public void vLineGrid(){
			vTmpLinesList = new ArrayList();
			Vector3f v1 = new Vector3f(vCameraX,vCameraY,vCameraZ);
			Vector3f v2 = mathUtil.step(vCameraAngle,vCameraLook,m3dCursorDistance);
			v2 = mathUtil.add(v1,v2);

			if(editorMOption == 1){
				v2 = mathUtil.crossX(vCameraAngle, vCameraLook, v1, editorXAxis);
			}

			//cursor
			float height = 1f/2f;
			float width = 1f/2f;
			Vector3f v3 = mathUtil.step(vCameraAngle+90f,0f,width);
			v3 = mathUtil.add(v3,v2);
			Vector3f v4 = mathUtil.step(vCameraAngle-90f,0f,width);
			v4 = mathUtil.add(v4,v2);
			vTmpLinesList.add(new vLine(new Vector3f(v3.x,v3.y,v3.z), new Vector3f(v4.x,v4.y,v4.z)));
			vTmpLinesList.add(new vLine(new Vector3f(v2.x,v2.y-height,v2.z), new Vector3f(v2.x,v2.y+height,v2.z)));

			float xWidth = 10/2;
			float yWidth = 10/2;
			float zWidth = 10/2;
			float lineInc = 1f;

			vTmpLinesList.add(new vLine(new Vector3f(Math.round(v2.x),v2.y-yWidth-1,Math.round(v2.z)), new Vector3f(Math.round(v2.x),v2.y+yWidth,Math.round(v2.z))));
			for(float x = v2.x-xWidth;x<v2.x+xWidth;x+=lineInc){
				vTmpLinesList.add(new vLine(new Vector3f(Math.round(x),0,v2.z-zWidth), new Vector3f(Math.round(x),0,v2.z+zWidth)));
			}
			for(float y = v2.y-yWidth;y<v2.y+yWidth;y+=lineInc){
				vTmpLinesList.add(new vLine(new Vector3f(Math.round(v2.x),Math.round(y),v2.z-zWidth), new Vector3f(Math.round(v2.x),Math.round(y),v2.z+zWidth)));
			}
			for(float y = v2.y-yWidth;y<v2.y+yWidth;y+=lineInc){
				vTmpLinesList.add(new vLine(new Vector3f(v2.x-xWidth,Math.round(y),Math.round(v2.z)), new Vector3f(v2.x+xWidth,Math.round(y),Math.round(v2.z))));
			}
			for(float z = v2.z-zWidth;z<v2.z+zWidth;z+=lineInc){
				//vTmpLinesList.add(new vLine(new Vector3f(v2.x-xWidth,(int) y,v2.z-zWidth), new Vector3f(v2.x,(int)y,v2.z+zWidth)));
			}
			vLines = vTmpLinesList;
		}

		public void vFillOtherTriangleObjects(){
			//add 3d text to triangles before pushed to vbo
			//OPTIONS MENU
			vScreenOptions();
			vAdd3dText(0,0,0,-18,1,"Sivx 3d Desktop",1,2);
			vDrawVertices();
			//p.pVadd3dRuler();
			//v3dCursor();
		}

		public void vDrawVertices(){
			boolean isMouseOver = false;
			if(p.pVectorClickTest())isMouseOver = true;

			while(pEVThreadPaused){
				uSleep(1);
			}

			mEVThreadPaused = true;

			for(int x = 0; x < editorVertices.size();x++){
				Vector3f tmpVector = (Vector3f) editorVertices.get(x);
				float[] phiTheta = mathUtil.phiTheta(tmpVector,cameraUtil.position());
				float height = 1f/2f;
				float width = 1f/2f;
				Vector3f v1 = mathUtil.step(phiTheta[0]+90f,0f,width);
				v1 = mathUtil.add(v1,tmpVector);
				Vector3f v2 = mathUtil.step(phiTheta[0]-90f,0f,width);
				v2 = mathUtil.add(v2,tmpVector);
				Vector3f v3 = mathUtil.clone(v1);
				Vector3f v4 = mathUtil.clone(v2);
				v1.y+=height;
				v2.y+=height;
				v3.y-=height;
				v4.y-=height;

				boolean isSelected = false;

				for(int y = 0; y < pShapeDesigner.vertices.size(); y++){
					Vector3f tmpVector2 = (Vector3f)pShapeDesigner.vertices.get(y);
					if(tmpVector.equals(tmpVector2))isSelected = true;
				}

				if(isMouseOver){
					if(x == editorVectorClicked)isSelected = true;
				}

				if(isSelected){
					vTriangle tmpTriangle1 = new vTriangle(v1, new Vector3f(0,0,0),
													v2, new Vector3f(0,0,0),
														v3,new Vector3f(0,1,0));

					vTriangle tmpTriangle2 = new vTriangle(v4, new Vector3f(0,0,0),
														v2, new Vector3f(0,0,0),
															v3,new Vector3f(0,1,0));

					vTmpTriangleList.add(tmpTriangle1);
					vTmpTriangleList.add(tmpTriangle2);
				}
				else{
					vTriangle tmpTriangle1 = new vTriangle(v1, new Vector3f(0,0,0),
													v2, new Vector3f(1,0,0),
														v3,new Vector3f(1,1,0));

					vTriangle tmpTriangle2 = new vTriangle(v4, new Vector3f(0,0,0),
														v2, new Vector3f(1,0,0),
															v3,new Vector3f(1,1,0));

					vTmpTriangleList.add(tmpTriangle1);
					vTmpTriangleList.add(tmpTriangle2);
				}

			}
			mEVThreadPaused = false;
		}

		public void vScreenOptions(){
			Vector3f v1 = new Vector3f(vCameraX,vCameraY,vCameraZ);
			Vector3f v2 = mathUtil.step(vCameraAngle,vCameraLook,10f);
			Vector3f v3 = mathUtil.step(vCameraAngle-90,0,.5f);
			v2 = mathUtil.add(v1,v2);
			v2 = mathUtil.add(v3,v2);
			vAdd3dText(v2.x,v2.y,v2.z,vCameraAngle-90,0,"Sivx 3D Desktop",.3f,2);
			/*
			if(editorMOption == 0)vAdd3dText(v2.x,v2.y,v2.z,vCameraAngle-90,0,"All Axis" + vFrameCount,.3f,2);
			else if(editorMOption == 1)vAdd3dText(v2.x,v2.y,v2.z,vCameraAngle-90,0,"X Axis",.3f,2);
			*/

		}

		public void v3dCursor(){
			Vector3f v1 = new Vector3f(vCameraX,vCameraY,vCameraZ);
			Vector3f v2 = mathUtil.step(vCameraAngle,vCameraLook,m3dCursorDistance);
			v2 = mathUtil.add(v1,v2);

			//float[] tp = mathUtil.phiTheta(v1,v2);
			//System.out.println(tp[0] + ", " + tp[1] + " " + vCameraAngle + ", " + vCameraLook);

			float height = 1f/2f;
			float width = 1f/2f;

			Vector3f v3 = mathUtil.step(vCameraAngle+90f,0f,width);
			v3 = mathUtil.add(v3,v2);
			Vector3f v4 = mathUtil.step(vCameraAngle-90f,0f,width);
			v4 = mathUtil.add(v4,v2);

			Vector3f v5 = mathUtil.clone(v3);
			Vector3f v6 = mathUtil.clone(v4);
			v3.y+=height;
			v4.y+=height;
			v5.y-=height;
			v6.y-=height;

			//top left half of letter
			vTriangle tmpTriangle1 = new vTriangle(v4, new Vector3f(0,0,0),
													v6, new Vector3f(1,0,0),
														v3,new Vector3f(1,1,0));

			//bottom right half of letter
			vTriangle tmpTriangle2 = new vTriangle(v3, new Vector3f(0,0,0),
													v6, new Vector3f(1,0,0),
														v5,new Vector3f(1,1,0));
			vTmpTriangleList.add(tmpTriangle1);
			vTmpTriangleList.add(tmpTriangle2);
		}

		public void vTriFillVbo(){
			if(vTriangles.size() != 0){
				vboVertices = ByteBuffer.allocateDirect(3 * 2 * (vTriangles.size()*3) * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
				vboIndices = ByteBuffer.allocateDirect((vTriangles.size()*3) * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
				for(int x = 0; x < vTriangles.size(); x++){
					((vTriangle) vTriangles.get(x)).vPut();
					vboIndices.put(x*3+0).put(x*3+1).put(x*3+2);
				}
				vboVertices.rewind();
				vboIndices.rewind();
				ARBBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 3 * 2 * (vTriangles.size()*3) * 4, ARBBufferObject.GL_DYNAMIC_DRAW_ARB);
				ARBBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, 3 * 2 * (vTriangles.size()*3) * 4, ARBBufferObject.GL_DYNAMIC_DRAW_ARB);
				if(!vVBOSETONCE)vVBOSETONCE=true;
			}
		}

		public void vLineFillVbo(){
			if(vLines.size() != 0){
				vboLineVertices = ByteBuffer.allocateDirect(3 * (vLines.size()*2) * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
				vboLineIndices = ByteBuffer.allocateDirect((vLines.size()*2) * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
				for(int x = 0; x < vLines.size(); x++){
					((vLine) vLines.get(x)).vPut();
					vboLineIndices.put(x*2+0).put(x*2+1);
				}
				vboLineVertices.rewind();
				vboLineIndices.rewind();
				ARBBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 3 * (vLines.size()*2) * 4, ARBBufferObject.GL_DYNAMIC_DRAW_ARB);
				ARBBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, 3 * (vLines.size()*2) * 4, ARBBufferObject.GL_DYNAMIC_DRAW_ARB);
				if(!vVBOSETONCE)vVBOSETONCE=true;
			}
		}

		public void vFillTriDemo(){
			//new vTriangle(new Vector3f(-10,-6,-10),textureUtil.c(0,0),new Vector3f(-10,-6,10),textureUtil.c(0,1), new Vector3f(10,-6,10), textureUtil.c(0,2),0f);
			//new vTriangle(new Vector3f(-10,-6,-10),textureUtil.c(0,0),new Vector3f(10,-6,-10),textureUtil.c(0,3), new Vector3f(10,-6,10), textureUtil.c(0,2),0f);

			//vTriPrismTower(new Vector3f(0f,0f,0f),10);

			int amount = 10;
			for(int x = 0; x < amount; x++){
				vShapeCube(new Vector3f(0,x*2+10,0),1);
			}

			vPlatform(new Vector3f(0f,-5f,0f));

			//line fill
			vLines.add(new vLine(new Vector3f(0,0,0), new Vector3f(0,10,0)));
			/*vLines.add(new vLine(new Vector3f(0,0,0), new Vector3f(0,10,10)));
			vLines.add(new vLine(new Vector3f(0,4,0), new Vector3f(10,0,0)));
			vLines.add(new vLine(new Vector3f(10,1,0), new Vector3f(0,5,0)));
			vLines.add(new vLine(new Vector3f(1,10,0), new Vector3f(5,3,5)));*/

			//vFillOthserTriangleObjects();
		}

		public void vPlatform(Vector3f v){
			float length = 100/2f;
			float width = 100/2f;
			float height = 1/2f;

			List<Vector3f> vertices = new ArrayList<Vector3f>();

			vertices.add(new Vector3f(-length,height,-width));
			vertices.add(new Vector3f(-length,height,width));
			vertices.add(new Vector3f(length,height,width));
			vertices.add(new Vector3f(length,height,-width));

			vertices.add(new Vector3f(-length,-height,-width));
			vertices.add(new Vector3f(-length,-height,width));
			vertices.add(new Vector3f(length,-height,width));
			vertices.add(new Vector3f(length,-height,-width));

			p.pAddT(vertices, v, 0);
		}

		public void vAdd3dText(float x, float y, float z, float heading, float lookupdown, String text, float size, int fontSpot){
			Vector3f position = mathUtil.step(heading,lookupdown,size);
			//System.out.println(position.x + " " + position.y + " " + position.z);
			for(int xx = 0; xx < text.length(); xx++){
				char car = text.charAt(xx);//65-90
				int charVal = (int)car;
				float valueOnChart = charVal - 32;

				if(charVal == 9){//Tab key
					valueOnChart = 0f;
					String temp = text.substring(xx+1, text.length());
					xx+=2;
					vAdd3dText(x+(xx-1)*position.x,y+(xx-1)*position.y, z+(xx-1)*position.z, heading, lookupdown, temp, size,fontSpot);
					xx = text.length();//end for loop
				}
				else{
					int row = (int) (valueOnChart/16f);
					int column = (int)(valueOnChart % 16f);

					Vector3f tmpSpot = textureUtil.map.getSpot(2);
					float offset = 1f/(16f*textureUtil.map.size);

					//System.out.println(tmpSpot.x + " " + tmpSpot.y + " " + offset);


					//top left half of letter
					vTriangle tmpTriangle1 = new vTriangle(new Vector3f(x+position.x*xx,y+size,z+position.z*xx), new Vector3f(tmpSpot.x+offset*(column),tmpSpot.y+offset*2*(row),0),
															new Vector3f(x+position.x*xx+position.x,y+size,z+position.z*xx+position.z),new Vector3f(tmpSpot.x+offset*(column+1),tmpSpot.y+offset*2*(row),0),
																new Vector3f(x+position.x*xx,y+0,z+position.z*xx),new Vector3f(tmpSpot.x+offset*(column),tmpSpot.y+offset*2*(row+1),0));

					//bottom right half of letter
					vTriangle tmpTriangle2 = new vTriangle(new Vector3f(x+position.x*xx,y+0,z+position.z*xx), new Vector3f(tmpSpot.x+offset*(column),tmpSpot.y+offset*2*(row+1),0),
															new Vector3f(x+position.x*xx+position.x,y+0,z+position.z*xx+position.z),new Vector3f(tmpSpot.x+offset*(column+1),tmpSpot.y+offset*2*(row+1),0),
																new Vector3f(x+position.x*xx+position.x,y+size,z+position.z*xx+position.z),new Vector3f(tmpSpot.x+offset*(column+1),tmpSpot.y+offset*2*(row),0));
					vTmpTriangleList.add(tmpTriangle1);
					vTmpTriangleList.add(tmpTriangle2);
						/*
			        	GL11.glTexCoord2f(.0313f*(column),.0625f*(row+1));GL11.glVertex3f (0f,-size,0f);
						GL11.glTexCoord2f(.0313f*(column+1),.0625f*(row+1));GL11.glVertex3f (size,-size,0f);
						GL11.glTexCoord2f(.0313f*(column+1),.0625f*(row));GL11.glVertex3f (size, 0f,0f);
						GL11.glTexCoord2f(.0313f*(column),.0625f*(row));GL11.glVertex3f (0f, 0f,0f);*/
				}
			}
		}

		public void vTriPrismTower(Vector3f v, int amount){
			for(int x = 0; x < amount; x++){
				Vector3f tmpVector = (Vector3f) v.clone();
				tmpVector.y = x * 2f;
				int xRand = 2;
				int zRand = 2;
				float xOff = (float)(Math.random()*xRand);
				float zOff = (float)(Math.random()*zRand);
				tmpVector.x += xOff;
				tmpVector.z += zOff;
				vTriPrism(tmpVector);
			}
		}

		public void vTriPrism(Vector3f v){

		}

		public void vShapeCube(Vector3f v, int mass){
			float size = 1f;
			List<Vector3f> vertices = new ArrayList<Vector3f>();
			//top
			vertices.add(new Vector3f(-size,size,-size));
			vertices.add(new Vector3f(-size,size,size));
			vertices.add(new Vector3f(size,size,size));
			vertices.add(new Vector3f(size,size,-size));
			//bottom
			vertices.add(new Vector3f(-size,-size,-size));
			vertices.add(new Vector3f(-size,-size,size));
			vertices.add(new Vector3f(size,-size,size));
			vertices.add(new Vector3f(size,-size,-size));

			p.pAddT(vertices, v, mass);
		}

		public void vPause(){
			vThreadPausedHint = true;

			while(vThreadPausedHint){
				uSleep(1);
			}
		}

		public void vUnpause(){
			vThreadPaused = false;
		}
	}

	public class m implements Runnable{
		public m(){(new Thread(this)).start();m=this;}
		public void run(){
			Mouse.setGrabbed(true);
			while(true){
				while(Display.isCreated() && Display.isActive()){
					float dx = Mouse.getDX();
					float dy = Mouse.getDY();
					if(dx != 0)vCameraAngle-=dx*.1f;
					if(dy != 0)vCameraLook-=dy*.1f;
					if(vCameraLook > 90f)vCameraLook = 90f;
					else if(vCameraLook < -90f)vCameraLook = -90f;

					if(mScreen == 1){
						if(Mouse.isButtonDown(1)){
							p.pRightClick();
						}
					}
					if(mScreen == 2){
						if(Mouse.isButtonDown(0)){
							while(mEVThreadPaused){
								uSleep(1);
							}

							if(p.pVectorClickTest()){
								pEVThreadPaused=true;
								Vector3f tmpVector = (Vector3f) editorVertices.get(editorVectorClicked);
								editorVertices.remove(editorVectorClicked);
								editorVertices.add(tmpVector);
								pShapeDesigner.addPoint(tmpVector);
							}
							else{
								pEVThreadPaused=true;
								//shapeDesigner, left click add point
								Vector3f v1 = new Vector3f(vCameraX,vCameraY,vCameraZ);
								Vector3f v2 = mathUtil.step(vCameraAngle,vCameraLook,m3dCursorDistance);
								v2 = mathUtil.add(v1,v2);

								if(editorMOption == 1){
									v2 = mathUtil.crossX(vCameraAngle, vCameraLook, v1, editorXAxis);
								}

								pShapeDesigner.addPoint(v2);
								editorVertices.add(new Vector3f(v2));
								//System.out.println(v2.x + ", " + v2.y + ", " + v2.z);
							}
							pEVThreadPaused=false;
							while(Mouse.isButtonDown(0)){}
						}

						if(Mouse.isButtonDown(1)){
							p.pVectorClickTest();
							while(Mouse.isButtonDown(1)){}
						}

						ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
						Controller[] ca = ce.getControllers();

						//System.out.println(ca[1].getName());
						Component[] components = ca[0].getComponents();
						ca[0].poll();
						//6 is mouse4  which is # 3, 7 is mouse5 which is #4

						//System.out.println(ca[0].getName());

						if(components[6].getPollData() == 1){
							int dw = Mouse.getDWheel();

							if(dw>0){
								editorMOption++;
							}
							else if(dw<0){
								editorMOption--;
							}
							if(editorMOption>editorMLast)editorMOption = 0;
							else if(editorMOption<0)editorMOption=editorMLast;

							if(editorMOption == 1){
								Vector3f v1 = new Vector3f(vCameraX,vCameraY,vCameraZ);
								Vector3f v2 = mathUtil.step(vCameraAngle,vCameraLook,m3dCursorDistance);
								v2 = mathUtil.add(v1,v2);
								editorXAxis = v2.x;
							}
						}
						else{
							int dw = Mouse.getDWheel();

							if(dw>0){
								m3dCursorDistance++;
							}
							else if(dw<0){
								m3dCursorDistance--;
							}
						}
					}

					uSleep(30);
				}
				uSleep(1000);
			}
		}
	}

	public class k implements Runnable{
		public k(){(new Thread(this)).start();}
		public void run(){
			while(true){
				while(Display.isCreated() && Display.isActive()){
					if(Keyboard.isKeyDown(Keyboard.KEY_F1))System.exit(0);
					if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)){
						pShapeDesigner.undo();
						while(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE));
					}
					if(Keyboard.isKeyDown(Keyboard.KEY_W)) {
						Vector3f tempVector = mathUtil.step(vCameraAngle,0f,.5f);
						vCameraX += tempVector.x;
						vCameraY += tempVector.y;
						vCameraZ += tempVector.z;
	        		}
	        		if(Keyboard.isKeyDown(Keyboard.KEY_S)) {
						Vector3f tempVector = mathUtil.step(-180+vCameraAngle,0f,.5f);
						vCameraX += tempVector.x;
						vCameraY += tempVector.y;
						vCameraZ += tempVector.z;
	        		}
	        		if(Keyboard.isKeyDown(Keyboard.KEY_A)) {
						Vector3f tempVector = mathUtil.step(90+vCameraAngle,0f,.5f);
						vCameraX += tempVector.x;
						vCameraY += tempVector.y;
						vCameraZ += tempVector.z;
	        		}
	        		if(Keyboard.isKeyDown(Keyboard.KEY_D)) {
						Vector3f tempVector = mathUtil.step(-90+vCameraAngle,0f,.5f);
						vCameraX += tempVector.x;
						vCameraY += tempVector.y;
						vCameraZ += tempVector.z;
	        		}
	        		if(mScreen == 2){
	        			if(Keyboard.isKeyDown(Keyboard.KEY_B)) {
	        				//System.out.println("BUILDING OUTPUT");
	        				//pShapeDesigner.build();
	        				while(Keyboard.isKeyDown(Keyboard.KEY_B));
	        			}
	        		}
					uSleep(30);
				}
				uSleep(1000);
			}
		}
	}

	public static class mathUtil{
		public static Vector3f step(float angle, float look, float distance){
			float radiansTheta = (float)((90+look)*(Math.PI/180));
			float radiansPhi = (float)((angle)*(Math.PI/180));
			float x = (float)((double)distance * Math.sin(radiansTheta) * Math.cos(radiansPhi));
	        float y = (float)((double)distance * Math.sin(radiansTheta) * Math.sin(radiansPhi));
	        float z = (float)((double)distance * Math.cos(radiansTheta));
	        return new Vector3f(-y, z, -x);
		}

		public Vector3f add(Vector3f v1, Vector3f v2){
			return new Vector3f(v1.x+v2.x,v1.y+v2.y,v1.z+v2.z);
		}

		public Vector3f sub(Vector3f v1, Vector3f v2){
			return new Vector3f(v1.x-v2.x,v1.y-v2.y,v1.z-v2.z);
		}

		public Vector3f clone(Vector3f v1){
			return new Vector3f(v1.x,v1.y,v1.z);
		}

		public float distance(Vector3f v1, Vector3f v2){
			float a = v2.x-v1.x;
			float b = v2.y-v1.y;
			float c = v2.z-v1.z;
			return (float) Math.sqrt(a*a+b*b+c*c);
		}

		public float distance2d(Vector3f v1, Vector3f v2){
			float a = v2.x-v1.x;
			float b = v2.y-v1.y;
			return (float) Math.sqrt(a*a+b*b);
		}

		public float[] phiTheta(Vector3f v1, Vector3f v2){
			float pi = 3.141593F;
			float distance = distance(v1,v2);
			Vector3f v3 = sub(v2,v1);
			float theta = (float)Math.acos((9.9999999999999995E-007D + (double)v3.y) / (double)distance);
	        float phi = (float)Math.atan((double)v3.z / ((double)v3.x + 9.9999999999999995E-007D));
	        if(Float.isNaN(theta))theta = 0f;
	        if(v3.x < 0.0F)phi += pi;
	        if(phi < 0.0F)phi += 2.0F * pi;
	        if(theta < 0.0F)theta += 2.0F * pi;
	        theta = (float)(theta*(180/Math.PI));
	        if(distance == v3.y)theta = 0;
	        if(v3.y == 0)theta = 90;
	        phi = (float)(phi*(180/Math.PI));
	        float tp[] = new float[2];
	        tp[1] = theta-90f;
	        tp[0] = 270f-phi;
	        return tp;
		}

		public void transformTriangle(vTriangle t){
			//build matrix to feed these few points.
			//t.v1 = ;
			//new v1 = ptransform + l1
			//Quat4f quatRot = new Quat4f();
			//t.pTransform.getRotation(quatRot);
			Matrix3f tmpMat = new Matrix3f();
			tmpMat.set(t.pDifference.basis);

			Vector3f v1 = add(t.pDifference.origin, t.translate);
			Vector3f v2 = add(t.pDifference.origin, t.translate);
			Vector3f v3 = add(t.pDifference.origin, t.translate);

			Vector3f r1 = (Vector3f) t.l1.clone();
			Vector3f r2 = (Vector3f) t.l2.clone();
			Vector3f r3 = (Vector3f) t.l3.clone();

			tmpMat.transform(r1);
			tmpMat.transform(r2);
			tmpMat.transform(r3);

			t.v1 = add(v1,r1);
			t.v2 = add(v2,r2);
			t.v3 = add(v3,r3);

			//t.v1 = add(v1,r1);
			//t.v2 = add(v2,r2);
			//t.v3 = add(v3,r3);

			//t.pTransform.transform(t.v1);
			//t.pTransform.transform(t.v2);
			//t.pTransform.transform(t.v3);
			//transforming
			//t.v1 = add(t.pTransform.origin, t.v1);
			//t.v1 = add(t.translate, t.v1);
			//t.v2 = add(t.pTransform.origin, t.v2);
			//t.v2 = add(t.translate, t.v2);
			//t.v3 = add(t.pTransform.origin, t.v3);
			//t.v3 = add(t.translate, t.v3);
		}

		public Vector3f crossX(float angle, float look, Vector3f position, float x){
			Vector3f v1 = step(angle,look,1000);
			Vector3f step1 = step(angle,look,1);

			Vector3f v2 = add(v1,position);

			float d1 = distance(v2,position);
			float d2 = distance2d(v2,position);
			float scale = d1/d2;

			float slope = slope(v2,position);
			float b = position.y - slope*position.x;
			float y = x*slope + b;

			float d3 = distance2d(new Vector3f(x,y,0),position);
			float multiplier = d3 * scale;

			Vector3f spot = new Vector3f(step1.x*multiplier, step1.y*multiplier,step1.z*multiplier);
			return add(spot,position);
		}

		public void intersectX(Vector3f v1, Vector3f v2, float x){

		}

		public float slope(Vector3f v1, Vector3f v2){
			return (v1.y-v2.y)/(v1.x-v2.x);
		}
	}

	public class cameraUtil{
		public Vector3f position(){
			return new Vector3f(vCameraX,vCameraY,vCameraZ);
		}
	}

	public class pHullShape {
		public pHullShape ( HullResult hullResult ){
			for(int x = 0 ; x < hullResult.outputVertices.size() ; x++){
				Vector3f tmpVtx = new Vector3f(hullResult.outputVertices.get(x));
				vtx.add(tmpVtx);
			}
			for(int x = 0 ; x < hullResult.indices.size() ; x++){
				int tmpIdx = new Integer(hullResult.indices.get(x));
				idx.add(tmpIdx);
			}
		}
		IntArrayList idx = new IntArrayList();
		List<Vector3f> vtx = new ArrayList();
	}

	public class pShapeDesigner{
		public pShapeDesigner(){pShapeDesigner = this;}

		public void addPoint(Vector3f v3f){
			if(vertices.size() < 3)vertices.add(v3f);
			if(vertices.size() == 3)build();
		}

		public void undo(){
			if(vertices.size() > 0){
				//System.out.println("VERT");
				vertices.remove(vertices.size()-1);
				//System.out.println("EDIT");
				editorVertices.remove(editorVertices.size()-1);
			}
		}

		public void build(){
			if(vertices.size()==3){
				p.pAddTriag(vertices, new Vector3f(), 0);//points,translate,gravity
				vertices=new ArrayList();
				selectedVertices=new ArrayList();

				while(editorVertices.size() >= 3)editorVertices.remove(0);
			}
			else{
				//System.out.println("need more points");
			}
		}

		int selected = 0;
		List<Vector3f> vertices = new ArrayList<Vector3f>();
	}

	public class vTriangle{
		public void set(Vector3f v1, Vector3f t1, Vector3f v2,Vector3f t2, Vector3f v3, Vector3f t3){
			this.v1 = v1;
			this.v2 = v2;
			this.v3 = v3;
			this.t1 = t1;
			this.t2 = t2;
			this.t3 = t3;
			l1 = (Vector3f) this.v1.clone();
			l2 = (Vector3f) this.v2.clone();
			l3 = (Vector3f) this.v3.clone();
			//vRefreshVBO = true;
		}
		public vTriangle(Vector3f v1, Vector3f t1, Vector3f v2,Vector3f t2, Vector3f v3, Vector3f t3){
			this.v1 = v1;
			this.v2 = v2;
			this.v3 = v3;
			this.t1 = t1;
			this.t2 = t2;
			this.t3 = t3;
			l1 = (Vector3f) this.v1.clone();
			l2 = (Vector3f) this.v2.clone();
			l3 = (Vector3f) this.v3.clone();
			//vRefreshVBO = true;
		}

		public void vPut(){
			vboVertices.put(v1.x).put(v1.y).put(v1.z).put(t1.x).put(t1.y).put(v2.x).put(v2.y).put(v2.z).put(t2.x).put(t2.y).put(v3.x).put(v3.y).put(v3.z).put(t3.x).put(t3.y);
		}
		Vector3f l1,l2,l3;//local points
		Vector3f r1,r2,r3;//extra: local to start, to save computing time.!!cant change origin in object shape!!
		Vector3f v1,v2,v3;//used for being sent to vbo
		Vector3f t1,t2,t3;//texture points
		Transform pDifference;
		Vector3f translate = new Vector3f(0f,0f,0f);
	}

	public class vLine{
		public void set(Vector3f v1, Vector3f v2){
			this.v1 = v1;
			this.v2 = v2;
			l1 = (Vector3f) this.v1.clone();
			l2 = (Vector3f) this.v2.clone();
			//vRefreshVBO = true;
		}
		public vLine(Vector3f v1, Vector3f v2){
			this.v1 = v1;
			this.v2 = v2;
			//vRefreshVBO = true;
		}

		public void vPut(){
			vboLineVertices.put(v1.x).put(v1.y).put(v1.z).put(v2.x).put(v2.y).put(v2.z);
		}
		Vector3f l1,l2,l3;//local points
		Vector3f r1,r2,r3;//extra: local to start, to save computing time.!!cant change origin in object shape!!
		Vector3f v1,v2,v3;//used for being sent to vbo
		Vector3f t1,t2,t3;//texture points
		Transform pDifference;
		Vector3f translate = new Vector3f(0f,0f,0f);
	}

	public class textureUtil{
		public void bind(int id){
			((t)t.get(id)).texture.bind();
		}
		public Vector3f c(int id, int c){
			if(c == 0)return new Vector3f((1f/map.size)*((id+1f) % map.size-1f), (1f/map.size)*((int)id/map.size), 0f);
			if(c == 1)return new Vector3f((1f/map.size)*((id+1f) % map.size-1f), (1f/map.size)*((int)id/map.size)+(1f/map.size), 0f);
			if(c == 2)return new Vector3f((1f/map.size)*((id+1f) % map.size-1f)+(1f/map.size), (1f/map.size)*((int)id/map.size)+(1f/map.size), 0f);
			if(c == 3)return new Vector3f((1f/map.size)*((id+1f) % map.size-1f)+(1f/map.size), (1f/map.size)*((int)id/map.size), 0f);
			return new Vector3f(0f,0f,0f);
		}

		public void addT(String s1, String s2){
			new t(s1, s2);
		}

		public class map{
			public void add(String filename){
				new texture(filename);
			}

			public Vector3f getSpot(int spot){//decimal spot
				Vector3f tmpVector = new Vector3f();
				tmpVector.x = ((float)spot % (float)size)/(float)size;
				tmpVector.y = ((float)spot / (float)size)/(float)size;
				return tmpVector;
				//return new Vector3f((spot2 % size)/size,(spot2 / size)/size,0f);
			}

			public void buildTextureMap(){
				//ONLY USING 128x128 textures atm.
				//Set Buffered image size, cycle through filling in textures plus setting offsets.
				for(int x = 1; x <= textures.size(); x++){
					if(x*x >= textures.size()){
						try{
							size = x;
							BufferedImage bufferedImage = new BufferedImage(x*128, x*128, BufferedImage.TYPE_INT_RGB);
							for(int y = 0; y < textures.size(); y++){
								BufferedImage image = ImageIO.read(new File(((texture)textures.get(y)).filename));
								((texture)textures.get(y)).posX = y % x;
								((texture)textures.get(y)).posY = y / x;
								for(int z = 0; z < 128; z++){
									for(int w = 0; w < 128; w++){
										bufferedImage.setRGB(((texture)textures.get(y)).posX * 128 + z, ((texture)textures.get(y)).posY * 128 + w, image.getRGB(z, w));
									}
								}
							}
							File file = new File("textureMap.png");
							ImageIO.write((RenderedImage)bufferedImage, "png", file);
						}catch(Exception e){}
						break;
					}
				}
			}
			public class texture{
				public texture(String filename){
					Image image = null;
					try {
						File file = new File(filename);
						image = ImageIO.read(file);
						this.filename = filename;
						tWidth = image.getWidth(null);
						tHeight = image.getHeight(null);
					}catch(Exception e){}
					textures.add(this);
					buildTextureMap();
				}
				String filename;
				int posX;
				int posY;
				double offsetX;
				double offsetY;
				int tWidth;
				int tHeight;
				int direction;
			}
			int size;
			ArrayList<texture> textures = new ArrayList<texture>();
		}

		//texture class /t
		public class t{
			public t(String fileType, String fileName){
				try{
					texture = TextureLoader.getTexture(fileType, new FileInputStream(fileName));
					id=tCounter++;
					t.add(this);
				}catch(Exception e){System.out.println("texture = textureLoader... failed");}
			}
			int id;
			Texture texture;
		}
		map map = new map();
	}

	//public utility methods are linked by a /u identifier.
	public void uSleep(int sleep){
		try{
			Thread.sleep(sleep);
		}catch(Exception e){}
	}

	//PHYSICS VARIABLES /p
	p p;
	float pNano = 1000000000f;//of a sec
	float pElapsedTime1;
	float pElapsedTime2;
	float pElapsedTime3;
	IGL pGl;

	CProfileIterator pProfileIterator;
	CProfileIterator pInterfaceProfileIterator;
	List<CollisionShape> pCollisionShapes = new ArrayList<CollisionShape>();
	List<CollisionShape> pInterfaceShapes = new ArrayList<CollisionShape>();
	BroadphaseInterface pOverlappingPairCache;
	BroadphaseInterface pInterfaceOverlappingPairCache;
	CollisionDispatcher pDispatcher;
	CollisionDispatcher pInterfaceDispatcher;
	ConstraintSolver pSolver;
	ConstraintSolver pInterfaceSolver;
	DefaultCollisionConfiguration pCollisionConfiguration;
	DefaultCollisionConfiguration pInterfaceCollisionConfiguration;
	DynamicsWorld pDynamicsWorld = null;
	DynamicsWorld pInterfaceWorld = null;
	ArrayList pShapeList = new ArrayList();//CollisionObject
	ArrayList pHullResultList = new ArrayList();
	ArrayList pTriangles = new ArrayList();
	boolean pPauseSimulation=false;
	boolean pSimulationPausedCheck=false;
	boolean pThreadPausedHint = false;
	boolean pThreadPaused = false;
	boolean pEVThreadPaused = false;

	final Transform pM = new Transform();
	ArrayList <RigidBody> pRigid = new ArrayList<RigidBody>();

	pShapeDesigner pShapeDesigner;

	//DISPLAY VARIABLES /v
	v v;
	DisplayMode vDisplayMode;
	boolean vFullscreen = false;
	int vScreenW = 800;
	int vScreenH = 600;
	int vDpi = 32;
	String vTitle = "CFPS";
	float vCameraX = 0f;
	float vCameraY = 2f;
	float vCameraZ = 20f;
	float vCameraLook = 0f;
	float vCameraAngle = 0f;
	boolean vRefreshVbo = false;
	boolean vVBOSETONCE = false;//prevent gfx from crashing before initialized
	ArrayList<vTriangle> vTriangles = new ArrayList<vTriangle>();
	ArrayList vTmpTriangleList = new ArrayList();
	ArrayList<vLine> vLines = new ArrayList<vLine>();
	ArrayList vTmpLinesList = new ArrayList();
	boolean vVBOPausedCheck = false;
	boolean vVBOPaused = false;
	boolean vThreadPausedHint = false;
	boolean vThreadPaused = false;
	int vFrameCount;//out of 100

	//TEXTURE VARIABLES /t
	ArrayList<textureUtil.t> t = new ArrayList<textureUtil.t>();
	int tCounter;

	//TEXTURE UTIL VARIABLES /texture
	textureUtil textureUtil = new textureUtil();

	mathUtil mathUtil = new mathUtil();

	cameraUtil cameraUtil = new cameraUtil();

	//mouse variables
	m m;
	int mScreen = 0;
	float m3dCursorDistance =3f;

	//VBO VARIABLES /vbo
	static float vboAngle;
	static int vboBuffer_id;
	static int vboIndices_buffer_id;

	static FloatBuffer vboVertices;
	static FloatBuffer vboLineVertices;
	static ByteBuffer vboMapped_buffer = null;
	static FloatBuffer vboMapped_float_buffer = null;
	static ByteBuffer vboLineMapped_buffer = null;
	static FloatBuffer vboLineMapped_float_buffer = null;
	static IntBuffer vboIndices;
	static IntBuffer vboLineIndices;
	static ByteBuffer vboMapped_indices_buffer = null;
	static IntBuffer vboMapped_indices_int_buffer = null;
	static ByteBuffer vboLineMapped_indices_buffer = null;
	static IntBuffer vboLineMapped_indices_int_buffer = null;

	//editor vars
	Vector3f editorCursor = new Vector3f();
	int editorMOption = 0;
	int editorMLast = 1;
	float editorXAxis = 0f;
	ArrayList editorVertices = new ArrayList();
	ArrayList editorMhint = new ArrayList();
	int editorVectorClicked = -1;
	boolean mEVThreadPausedHint = false;
	boolean mEVThreadPaused = false;
	ArrayList<Integer> selectedVertices = new ArrayList<Integer>();
}