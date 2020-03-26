/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir

import com.intellij.lang.ASTNode
import com.intellij.lang.LighterASTNode
import com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtModifierList

sealed class FirModifierList {
    abstract val modifiers: List<FirModifier<*>>
}

private val MODIFIER_KEYWORD_SET = TokenSet.orSet(KtTokens.SOFT_KEYWORDS, TokenSet.create(KtTokens.IN_KEYWORD, KtTokens.FUN_KEYWORD))

class FirPsiModifierList(val modifierList: KtModifierList) : FirModifierList() {
    override val modifiers: List<FirPsiModifier>
        get() = modifierList.node.getChildren(MODIFIER_KEYWORD_SET).map { node ->
            FirPsiModifier(node, node.elementType as KtModifierKeywordToken)
        }

}

class FirLightModifierList(val modifierList: LighterASTNode) : FirModifierList() {
    override val modifiers: List<FirLightModifier>
        get() = TODO()
}

sealed class FirModifier<Node : Any>(val node: Node, val token: KtModifierKeywordToken)

class FirPsiModifier(node: ASTNode, token: KtModifierKeywordToken) : FirModifier<ASTNode>(node, token)

class FirLightModifier(node: LighterASTNode, token: KtModifierKeywordToken) : FirModifier<LighterASTNode>(node, token)