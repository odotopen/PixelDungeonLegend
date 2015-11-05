/*
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.watabou.noosa;

public class Gizmo {
	
	public boolean exists;
	public boolean alive;
	public boolean active;
	private boolean visible;
	
	private Group parent;
	
	public Camera camera;
	
	public Gizmo() {
		exists	= true;
		alive	= true;
		active	= true;
		setVisible(true);
	}
	
	public void destroy() {
		parent = null;
	}
	
	public void update() {
	}
	
	public void draw() {
	}
	
	public void kill() {
		alive = false;
		exists = false;
	}
	
	// Not exactly opposite to "kill" method
	public void revive() {
		alive = true;
		exists = true;
	}
	
	public Camera camera() {
		if (camera != null) {
			return camera;
		} else if (parent != null) {
			return parent.camera();
		} else {
			return null;
		}
	}
	
	public boolean isVisible() {
		if (parent == null) {
			return getVisible();
		} else {
			return getVisible() && parent.isVisible();
		}
	}
	
	public boolean isActive() {
		if (parent == null) {
			return active;
		} else {
			return active && parent.isActive();
		}
	}
	
	public void killAndErase() {
		kill();
		if (parent != null) {
			parent.erase( this );
		}
	}
	
	public void remove() {
		if (parent != null) {
			parent.remove( this );
		}
	}

	public Group getParent() {
		return parent;
	}

	public void setParent(Group parent) {
		this.parent = parent;
	}

	public boolean getVisible() {
		return visible;
	}

	public boolean setVisible(boolean visible) {
		this.visible = visible;
		return visible;
	}
}
