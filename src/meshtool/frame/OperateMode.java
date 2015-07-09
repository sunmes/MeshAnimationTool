/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package meshtool.frame;

import com.badlogic.gdx.graphics.g3d.particles.influencers.RegionInfluencer;

/**
 * 操作类型
 */
public interface OperateMode {
    
    public enum RunMode{
        /**
         * 顶点编辑状态
         */
        VertexEdit,
        /**
         * 动画编辑状态
         */
        AnimateEdit,
        
    }

    public enum PointEditModeType {
        /**
         * 普通状态,选择工具
         */
        Normal,
        /**
         * 添加节点状态
         */
        AddVertex,
        /**
         * 删除节点状态
         */
        DeleteVertex,
        /**
         * 添加三角形状态
         */
        AddTriangle,
        /**
         * 删除三角形状态
         */
        DeleteTriangle,

    }

}
